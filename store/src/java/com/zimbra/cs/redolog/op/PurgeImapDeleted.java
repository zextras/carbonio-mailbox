// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class PurgeImapDeleted extends RedoableOp {

    public PurgeImapDeleted() {
        super(MailboxOperation.PurgeImapDeleted);
    }

    public PurgeImapDeleted(int mailboxId) {
        this();
        setMailboxId(mailboxId);
    }

    @Override
    protected String getPrintableData() {
        // no members to print
        return null;
    }

    @Override
    protected void serializeData(RedoLogOutput out) {
        // no members to serialize
    }

    @Override
    protected void deserializeData(RedoLogInput in) {
        // no members to deserialize
    }

    @Override
    public void redo() throws ServiceException {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.purgeImapDeleted(getOperationContext());
    }

    @Override
    public boolean isDeleteOp() {
        return true;
    }
}
