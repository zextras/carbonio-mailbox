// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxOperation;

public class UnlockItem extends LockItem {

    public UnlockItem() {
        super();
        mOperation = MailboxOperation.UnlockItem;
    }

    public UnlockItem(int mailboxId, int id, MailItem.Type type, String accountId) {
        super(mailboxId, id, type, accountId);
        mOperation = MailboxOperation.UnlockItem;
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.unlock(getOperationContext(), id, type, accountId);
    }
}
