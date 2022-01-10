// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class SetFolderDefaultView extends RedoableOp {

    private int mFolderId;
    private MailItem.Type defaultView;

    public SetFolderDefaultView() {
        super(MailboxOperation.SetFolderDefaultView);
        mFolderId = Mailbox.ID_AUTO_INCREMENT;
        defaultView = MailItem.Type.UNKNOWN;
    }

    public SetFolderDefaultView(int mailboxId, int folderId, MailItem.Type view) {
        this();
        setMailboxId(mailboxId);
        mFolderId = folderId;
        defaultView = view;
    }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("id=").append(mFolderId);
        sb.append(", view=").append(defaultView);
        return sb.toString();
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mFolderId);
        out.writeByte(defaultView.toByte());
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        mFolderId = in.readInt();
        defaultView = MailItem.Type.of(in.readByte());
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.setFolderDefaultView(getOperationContext(), mFolderId, defaultView);
    }
}
