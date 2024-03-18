// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.session;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.ZimbraNamespace;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.LruMap;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.iochannel.CrossServerNotification;
import com.zimbra.cs.iochannel.MessageChannel;
import com.zimbra.cs.iochannel.MessageChannelException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.soap.ZimbraSoapContext;

public class RemoteSoapSession extends SoapSession {
    /** Creates a <tt>SoapSession</tt> owned by the given account homed on
     *  a different server.  It thus cannot listen on its own {@link Mailbox}.
     * @see Session#register() */
    public RemoteSoapSession(ZimbraSoapContext zsc) {
        super(zsc);
        try {
            authUserCtxt = new ZimbraSoapContext(zsc);
        } catch (ServiceException e) {
        }
    }

    @Override
    public RemoteSoapSession register() throws ServiceException {
        super.register();
        if (MessageChannel.getInstance().isRunning()) {
            registerNotificationConnection(new CrossMailboxPushChannel());
        }
        return this;
    }

    @Override
    protected boolean isMailboxListener() {
        return false;
    }

    @Override
    public String getRemoteSessionId(Server server) {
        return null;
    }

    @Override
    public void putRefresh(Element ctxt, ZimbraSoapContext zsc) {
        ctxt.addUniqueElement(ZimbraNamespace.E_REFRESH);
    }

    @Override
    public Element putNotifications(Element ctxt, ZimbraSoapContext zsc, int lastSequence) {
        if (ctxt == null) {
            return null;
        }
        QueuedNotifications ntfn;
        synchronized (sentChanges) {
            if (!changes.hasNotifications()) {
                return null;
            }
            ntfn = changes;
            changes = new QueuedNotifications(ntfn.getSequence() + 1);
        }

        putQueuedNotifications(null, ntfn, ctxt, zsc);
        return ctxt;
    }

    private ZimbraSoapContext authUserCtxt;

    /* per account cache of recently sent notifications for deduping */
    private static final Map<String,LinkedList<String>> sentNotifications;

    static {
        sentNotifications = new LruMap<>(100);  // cache of 100 accounts
    }

    private class CrossMailboxPushChannel implements PushChannel {

        @Override
        public void closePushChannel() {
        }

        @Override
        public int getLastKnownSequence() {
            return 0;
        }

        @Override
        public ZimbraSoapContext getSoapContext() {
            return null;
        }

        @Override
        public boolean localChangesOnly() {
            return false;
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

        @Override
        public void notificationsReady() {
            CrossServerNotification ntfn;
            try {
                ntfn = CrossServerNotification.create(RemoteSoapSession.this, authUserCtxt);
            } catch (MessageChannelException e) {
                ZimbraLog.session.warn("unable to create CrossServerNotification", e);
                return;
            }
            if (!checkDuplicateNotification(ntfn)) {
                MessageChannel.getInstance().sendMessage(ntfn);
            }
        }

        private boolean checkDuplicateNotification(CrossServerNotification ntfn) {
            String msgHash;
            try {
                msgHash = ByteUtil.getDigest(ntfn.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                msgHash = ByteUtil.getDigest(ntfn.toString().getBytes());
            }
            String accountId = authUserCtxt.getAuthtokenAccountId();
            LinkedList<String> messageHashes;
            synchronized (sentNotifications) {
                messageHashes = sentNotifications.get(accountId);
                if (messageHashes == null) {
                    messageHashes = new LinkedList<>();
                    sentNotifications.put(accountId, messageHashes);
                }
            }
            synchronized (messageHashes) {
                if (!messageHashes.contains(msgHash)) {
                    // keep the last 10 notifications sent for this account
                    if (messageHashes.size() > 9) {
                        messageHashes.removeFirst();
                    }
                    messageHashes.add(msgHash);
                    return false;  // new notification
                }
            }
            return true;  // duplicate
        }
    }
}
