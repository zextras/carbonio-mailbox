// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.mail.WaitSetRequest;
import com.zimbra.soap.admin.type.AccountsAttrib;
import com.zimbra.soap.admin.type.WaitSetInfo;
import com.zimbra.soap.type.IdAndType;

/**
 * The base class defines shared functions, as well as any APIs which should be
 * package-private
 */
public abstract class WaitSetBase implements IWaitSet {
    protected final String mWaitSetId;
    protected final String mOwnerAccountId;
    protected final Set<MailItem.Type> defaultInterest;

    protected long mLastAccessedTime = -1;
    protected WaitSetCallback mCb = null;

    /**
     * List of errors (right now, only mailbox deletion notifications) to be sent
     */
    protected List<WaitSetError> mCurrentErrors = new ArrayList<>();
    protected List<WaitSetError> mSentErrors = new ArrayList<>();

    /** this is the signalled set data that is new (has never been sent) */
    protected HashSet<String /*accountId*/> mCurrentSignalledAccounts = Sets.newHashSet();
    protected HashSet<WaitSetSession> mCurrentSignalledSessions = Sets.newHashSet();
    protected Map<String /*accountId*/, PendingModifications> currentPendingModifications = Maps.newHashMap();

    /** this is the signalled set data that we've already sent, it just hasn't been acked yet */
    protected HashSet<String /*accountId*/> mSentSignalledAccounts = Sets.newHashSet();
    protected HashSet<WaitSetSession /*accountId*/> mSentSignalledSessions = Sets.newHashSet();
    protected Map<String /*accountId*/, PendingModifications> sentPendingModifications = Maps.newHashMap();

    protected abstract Map<String, WaitSetAccount> destroy();
    protected abstract int countSessions();
    protected abstract boolean cbSeqIsCurrent();
    protected abstract String toNextSeqNo();

    public long getLastAccessedTime() {
        return mLastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        mLastAccessedTime = lastAccessedTime;
    }

    @Override
    public Set<MailItem.Type> getDefaultInterest() {
        return defaultInterest;
    }

    @Override
    public String getOwnerAccountId() {
        return mOwnerAccountId;
    }

    @Override
    public String getWaitSetId() {
        return mWaitSetId;
    }

    protected synchronized WaitSetCallback getCb() { return mCb; }

    /**
     * Cancel any existing callback
     */
    protected synchronized void cancelExistingCB() {
        if (mCb != null) {
            // cancel the existing waiter
            mCb.dataReadySetCanceled(this, "");
            ZimbraLog.session.trace("WaitSetBase.cancelExistingCB - setting mCb null");
            mCb = null;
            mLastAccessedTime = System.currentTimeMillis();
        }
    }

    /**
     * Called to signal that the supplied WaitSetCallback should not be notified of any more changes
     * @param myCb - the callback that will no longer accept change notifications
     */
    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public synchronized void doneWaiting(WaitSetCallback myCb) {
        mLastAccessedTime = System.currentTimeMillis();
        boolean sameObject = (mCb == null);
        if (sameObject) {
            return;
        }
        if ((myCb == null) || (mCb == myCb)) {
            ZimbraLog.session.debug("WaitSetBase.doneWaiting - setting mCb null");
            mCb = null;
        } else {
            // This happens when the callers request has been canceled by a newer request
            ZimbraLog.session.debug("WaitSetBase.doneWaiting - saved callback NOT ours so NOT making null");
        }
    }


    protected WaitSetBase(String ownerAccountId, String waitSetId, Set<MailItem.Type> defaultInterest) {
        mOwnerAccountId = ownerAccountId;
        mWaitSetId = waitSetId;
        this.defaultInterest = defaultInterest;
    }

    protected synchronized void trySendData() {
        if (mCb == null) {
            ZimbraLog.session.trace("WaitSetBase.trySendData - no callback listening");
            return;
        }

        ZimbraLog.session.trace("WaitSetBase.trySendData 1 cb=%s", mCb);
        boolean cbIsCurrent = cbSeqIsCurrent();

        if (cbIsCurrent) {
            mSentSignalledAccounts.clear();
            mSentSignalledSessions.clear();
            sentPendingModifications.clear();
            mSentErrors.clear();
        }

        /////////////////////
        // Cases:
        //
        // CB up to date
        //   AND Current empty --> WAIT
        //   AND Current NOT empty --> SEND
        //
        // CB not up to date
        //   AND Current empty AND Sent empty --> WAIT
        //   AND (Current NOT empty OR Sent NOT empty) --> SEND BOTH
        //
        // ...simplifies to:
        //        send if Current NOT empty OR
        //                (CB not up to date AND Sent not empty)
        //
        if ((mCurrentSignalledAccounts.size() > 0 || mCurrentErrors.size() > 0) ||
                        (!cbIsCurrent && (mSentSignalledSessions.size() > 0 || mSentErrors.size() > 0 || mSentSignalledAccounts.size() > 0))) {
            // if sent is empty, then just swap sent,current instead of copying
            if (mSentSignalledAccounts.size() == 0) {
                ZimbraLog.session.trace("WaitSetBase.trySendData 2a");
                // SWAP sent <->current
                HashSet<String> tempAccounts = mCurrentSignalledAccounts;
                mCurrentSignalledAccounts = mSentSignalledAccounts;
                mSentSignalledAccounts = tempAccounts;
                HashSet<WaitSetSession> tempSessions = mCurrentSignalledSessions;
                mCurrentSignalledSessions = mSentSignalledSessions;
                mSentSignalledSessions = tempSessions;
                Map<String, PendingModifications> tempNotifications = currentPendingModifications;
                currentPendingModifications = sentPendingModifications;
                sentPendingModifications = tempNotifications;
            } else {
                ZimbraLog.session.trace("WaitSetBase.trySendData 2b");
                assert(!cbIsCurrent);
                mSentSignalledAccounts.addAll(mCurrentSignalledAccounts);
                mCurrentSignalledAccounts.clear();
                mSentSignalledSessions.addAll(mCurrentSignalledSessions);
                mCurrentSignalledSessions.clear();
                sentPendingModifications.putAll(currentPendingModifications);
                currentPendingModifications.clear();
            }

            // error list
            mSentErrors.addAll(mCurrentErrors);
            mCurrentErrors.clear();

            assert(mSentSignalledAccounts.size() > 0 || mSentErrors.size() > 0);
            ZimbraLog.session.trace("WaitSetBase.trySendData 3");
            mCb.dataReady(this, toNextSeqNo(), false, mSentErrors, mSentSignalledSessions, mSentSignalledAccounts, sentPendingModifications);
            mCb = null;
            mLastAccessedTime = System.currentTimeMillis();
        }
        ZimbraLog.session.trace("WaitSetBase.trySendData done");
    }

    @Override
    public synchronized WaitSetInfo handleQuery() {
        WaitSetInfo info = WaitSetInfo.createForWaitSetIdOwnerInterestsLastAccessDate(mWaitSetId, mOwnerAccountId,
                WaitSetRequest.expandInterestStr(defaultInterest), mLastAccessedTime);

        if (mCurrentErrors.size() > 0) {
            for (WaitSetError error : mCurrentErrors) {
                info.addError(new IdAndType(error.accountId, error.error.name()));
            }
        }

        // signaled accounts
        if (mCurrentSignalledAccounts.size() > 0) {
            StringBuilder signaledStr = new StringBuilder();
            for (String accountId : mCurrentSignalledAccounts) {
                if (signaledStr.length() > 0)
                    signaledStr.append(",");
                signaledStr.append(accountId);
            }
            info.setSignalledAccounts(new AccountsAttrib(signaledStr.toString()));
        }
        return info;
    }

    protected synchronized void signalError(WaitSetError err) {
        mCurrentErrors.add(err);
        trySendData();
    }

    protected synchronized void addChangeFolderIds(Map<String, Set<Integer>> folderIdsMap,
            String acctId, Set<Integer> changedFolderIds) {
        Set<Integer> fids = folderIdsMap.get(acctId);
        if (fids == null) {
            fids = Sets.newHashSet();
            folderIdsMap.put(acctId, fids);
        }
        fids.addAll(changedFolderIds);
    }

    protected synchronized void addMods(Map<String, PendingModifications> mods, String acctId, PendingModifications mod) {
        mods.put(acctId, mod);
    }
}
