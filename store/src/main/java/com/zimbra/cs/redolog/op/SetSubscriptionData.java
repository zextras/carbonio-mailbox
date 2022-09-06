// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 12, 2005
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class SetSubscriptionData extends RedoableOp {

  private int mFolderId;
  private long mLastItemDate;
  private String mLastItemGuid;

  public SetSubscriptionData() {
    super(MailboxOperation.SetSubscriptionData);
    mFolderId = Mailbox.ID_AUTO_INCREMENT;
    mLastItemGuid = "";
  }

  public SetSubscriptionData(int mailboxId, int folderId, long date, String guid) {
    this();
    setMailboxId(mailboxId);
    mFolderId = folderId;
    mLastItemDate = date;
    mLastItemGuid = guid == null ? "" : guid;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(mFolderId);
    sb.append(", date=").append(mLastItemDate);
    sb.append(", guid=").append(mLastItemGuid);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mFolderId);
    out.writeLong(mLastItemDate);
    out.writeUTF(mLastItemGuid);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mFolderId = in.readInt();
    mLastItemDate = in.readLong();
    mLastItemGuid = in.readUTF();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.setSubscriptionData(getOperationContext(), mFolderId, mLastItemDate, mLastItemGuid);
  }
}
