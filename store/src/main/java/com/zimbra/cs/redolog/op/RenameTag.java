// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

/**
 * @since 2004. 7. 21.
 */
public class RenameTag extends RenameItem {

  public RenameTag() {
    super();
    mOperation = MailboxOperation.RenameTag;
    type = MailItem.Type.TAG;
    mFolderId = Mailbox.ID_FOLDER_TAGS;
  }

  public RenameTag(int mailboxId, int tagId, String name) {
    super(mailboxId, tagId, MailItem.Type.TAG, name, Mailbox.ID_FOLDER_TAGS);
    mOperation = MailboxOperation.RenameTag;
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeUTF(mName);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    mName = in.readUTF();
  }
}
