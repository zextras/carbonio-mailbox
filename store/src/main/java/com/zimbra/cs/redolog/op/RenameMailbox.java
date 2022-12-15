// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.util.AccountUtil;

public class RenameMailbox extends RedoableOp {

    private String mNewName;
    private String mOldName;

    public RenameMailbox() {
        super(MailboxOperation.RenameMailbox);
    }

    public RenameMailbox(int mailboxId, String oldName, String newName) {
        this();
        setMailboxId(mailboxId);
        mNewName = newName;
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeUTF(mNewName);
        if (getVersion().atLeast(1,25))
            out.writeUTF(mOldName);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mNewName = in.readUTF();
        if (getVersion().atLeast(1,25))
            mOldName = in.readUTF();
    }

    @Override protected String getPrintableData() {
        return "newName=" + mNewName+" oldName="+mOldName;
    }

    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        // Redo the rename only if the current account name matches mOldName.  This prevents renaming an account
        // that is restored as a copy. (-ca option of zmrestore)
        if (mNewName != null && AccountUtil.addressMatchesAccount(mbox.getAccount(), mOldName)) {
            mbox.renameMailbox(mOldName, mNewName);
        }
    }
}
