// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @since 2004. 12. 13.
 */
public class RenameFolder extends RenameItem {

    public RenameFolder() {
        super();
        mOperation = MailboxOperation.RenameFolder;
        type = MailItem.Type.FOLDER;
    }

    public RenameFolder(int mailboxId, int id, String name, int parentId) {
        super(mailboxId, id, MailItem.Type.FOLDER, name, parentId);
        mOperation = MailboxOperation.RenameFolder;
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
        out.writeInt(mFolderId);
        out.writeUTF(mName);
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        mFolderId = in.readInt();
        mName = in.readUTF();
    }
}
