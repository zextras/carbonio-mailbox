// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.session;

import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.soap.admin.type.WaitSetInfo;

/**
 * WaitSet: scalable mechanism for listening for changes to one or many accounts */
public interface IWaitSet {

    /**
     * WaitMultipleAccounts:  optionally modifies the wait set and checks
     * for any notifications.  If block=1 and there are no notificatins, then
     * this API will BLOCK until there is data.
     *
     * Client should always set 'seq' to be the highest known value it has
     * received from the server.  The server will use this information to
     * retransmit lost data.
     *
     * If the client sends a last known sync token then the notification is
     * calculated by comparing the accounts current token with the client's
     * last known.
     *
     * If the client does not send a last known sync token, then notification
     * is based on change since last Wait (or change since <add> if this
     * is the first time Wait has been called with the account)
     *
     * IMPORTANT NOTE: Caller *must* call doneWaiting() when done waiting for the callback
     *
     * @param cb
     * @param lastKnownSeqNo
     * @param block
     * @param addAccounts
     * @param updateAccounts
     * @param removeAccounts
     * @return
     * @throws ServiceException
     */
    List<WaitSetError> doWait(WaitSetCallback cb, String lastKnownSeqNo,
        List<WaitSetAccount> addAccounts, List<WaitSetAccount> updateAccounts)
        throws ServiceException;

    /**
     * Handle removes separately from the main doWait API -- this is because removes
     * must be run without holding the WS lock (due to deadlock issues)
     *
     * @return
     */
    List<WaitSetError> removeAccounts(List<String> removeAccounts);

    /**
     * Called to signal that the supplied WaitSetCallback should not be notified of any more changes
     * @param myCb - the callback that will no longer accept change notifications
     */
    void doneWaiting(WaitSetCallback myCb);

    /**
     * Just a helper: the 'default interest' is set when the WaitSet is created,
     * and subsequent requests can access it when creating/updating WaitSetAccounts
     * if the client didn't specify one with the update.
     */
    Set<MailItem.Type> getDefaultInterest();

    /**
     * @return The accountID of the owner/creator
     */
    String getOwnerAccountId();

    /**
     * @return the id of this wait set
     */
    String getWaitSetId();

    /** Handle a QueryWaitSet request by encoding all of our internal data into a JAXB object for the response */
    WaitSetInfo handleQuery();
}
