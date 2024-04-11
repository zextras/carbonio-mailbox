// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.ArrayList;
import java.util.List;

/**
 * Mailbox maintenance context.
 */
public final class MailboxMaintenance {

    private final String accountId;
    private final int mailboxId;
    private Mailbox mailbox;
    private List<Thread> allowedThreads;
    private boolean nestedAllowed = false;
    private boolean inner = false;

    MailboxMaintenance(String acct, int id) {
        this(acct, id, null);
    }

    MailboxMaintenance(String acct, int id, Mailbox mbox) {
        accountId = acct.toLowerCase();
        mailboxId = id;
        mailbox = mbox;
        allowedThreads = new ArrayList<>();
        allowedThreads.add(Thread.currentThread());
    }

    String getAccountId() {
        return accountId;
    }

    int getMailboxId() {
        return mailboxId;
    }

    Mailbox getMailbox() {
        return mailbox;
    }

    void setMailbox(Mailbox mbox) {
        if (mbox.getId() == mailboxId && mbox.getAccountId().equalsIgnoreCase(accountId)) {
            mailbox = mbox;
        }
    }

    synchronized void registerOuterAllowedThread(Thread t) throws MailServiceException {
        if (inner) {
            throw MailServiceException.MAINTENANCE(mailboxId, "cannot add new maintenance thread when inner maintenance is already started");
        } else if (!nestedAllowed) {
            throw MailServiceException.MAINTENANCE(mailboxId, "cannot add outer maintenance thread when nested is not enabled");
        } else if (allowedThreads.size() > 0) {
            throw MailServiceException.MAINTENANCE(mailboxId, "cannot add multiple outer maintenance threads");
        }
        registerAllowedThread(t);
    }

    public synchronized void registerAllowedThread(Thread t) {
        allowedThreads.add(t);
    }

    synchronized void removeAllowedThread(Thread t) {
        allowedThreads.remove(t);
    }

    synchronized void setNestedAllowed(boolean allowed) {
        nestedAllowed = allowed;
    }

    synchronized void startInnerMaintenance() throws MailServiceException {
        if (inner) {
            throw MailServiceException.MAINTENANCE(mailboxId, "attempted to nest maintenance when already nested");
        } else if (!nestedAllowed || !canAccess()) {
            throw MailServiceException.MAINTENANCE(mailboxId, "attempted to nest maintenance when not allowed");
        }
        inner = true;
    }

    synchronized boolean endInnerMaintenance() {
        boolean set = inner;
        assert(nestedAllowed || !set);
        inner = false;
        return set;
    }

    synchronized boolean isNestedAllowed() {
        return nestedAllowed;
    }


    synchronized boolean canAccess() {
        return allowedThreads.contains(Thread.currentThread());
    }

    synchronized void markUnavailable()  {
        mailbox = null;
        inner = false;
        nestedAllowed = false;
        allowedThreads.clear();
    }

    void cacheMailbox(Mailbox mbox) {
        if (mbox.getId() == mailboxId && mbox.getAccountId().equalsIgnoreCase(accountId))
            mailbox = mbox;
    }
}
