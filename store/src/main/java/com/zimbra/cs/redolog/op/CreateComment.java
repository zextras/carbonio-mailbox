// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class CreateComment extends RedoableOp {

  private int mItemId;
  private String mUuid;
  private int mParentId;
  private String mCreatorId;
  private String mText;

  public CreateComment() {
    super(MailboxOperation.CreateComment);
  }

  public CreateComment(int mailboxId, int parentId, String text, String creatorId) {
    this();
    setMailboxId(mailboxId);
    mParentId = parentId;
    mText = text;
    mCreatorId = creatorId;
  }

  public int getItemId() {
    return mItemId;
  }

  public String getUuid() {
    return mUuid;
  }

  public void setItemIdAndUuid(int itemId, String uuid) {
    mItemId = itemId;
    mUuid = uuid;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("id=").append(mItemId);
    sb.append(", uuid=").append(mUuid);
    sb.append(", creator=").append(mCreatorId);
    sb.append(", text=").append(mText).append(", parentId=").append(mParentId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mItemId);
    if (getVersion().atLeast(1, 37)) {
      out.writeUTF(mUuid);
    }
    out.writeInt(mParentId);
    out.writeUTF(mCreatorId);
    out.writeUTF(mText);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mItemId = in.readInt();
    if (getVersion().atLeast(1, 37)) {
      mUuid = in.readUTF();
    }
    mParentId = in.readInt();
    mCreatorId = in.readUTF();
    mText = in.readUTF();
  }

  @Override
  public void redo() throws Exception {
    int mboxId = getMailboxId();
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(mboxId);
    mbox.createComment(getOperationContext(), mParentId, mText, mCreatorId);
  }
}
