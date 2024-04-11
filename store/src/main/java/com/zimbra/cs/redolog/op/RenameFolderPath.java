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
public class RenameFolderPath extends RenameItemPath {

    public RenameFolderPath() {
        mOperation = MailboxOperation.RenameFolderPath;
        mId = UNKNOWN_ID;
        type = MailItem.Type.FOLDER;
    }

    public RenameFolderPath(int mailboxId, int id, String path) {
        super(mailboxId, id, MailItem.Type.FOLDER, path);
        mOperation = MailboxOperation.RenameFolderPath;
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
        out.writeUTF(mPath);
        if (mParentIds != null) {
            out.writeInt(mParentIds.length);
          for (int mParentId : mParentIds) {
            out.writeInt(mParentId);
          }
        } else {
            out.writeInt(0);
        }
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        mPath = in.readUTF();
        int numParentIds = in.readInt();
        if (numParentIds > 0) {
            mParentIds = new int[numParentIds];
            for (int i = 0; i < numParentIds; i++) {
                mParentIds[i] = in.readInt();
            }
        }
    }
}
