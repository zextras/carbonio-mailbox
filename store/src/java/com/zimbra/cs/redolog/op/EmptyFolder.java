// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 6, 2005
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class EmptyFolder extends RedoableOp {

    private int mId;
    private boolean mSubfolders;


    public EmptyFolder() {
        super(MailboxOperation.EmptyFolder);
        mId = UNKNOWN_ID;
        mSubfolders = false;
    }

    public EmptyFolder(int mailboxId, int id, boolean subfolders) {
        this();
        setMailboxId(mailboxId);
        mId = id;
        mSubfolders = subfolders;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=");
        sb.append(mId).append(", subfolders=").append(mSubfolders);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
        out.writeBoolean(mSubfolders);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        mSubfolders = in.readBoolean();
    }

    @Override public boolean isDeleteOp() {
        return true;
    }

    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.emptyFolder(getOperationContext(), mId, mSubfolders);
    }
}
