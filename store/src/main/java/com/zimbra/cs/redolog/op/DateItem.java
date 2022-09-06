// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

/**
 * @since Nov 12, 2005
 */
public class DateItem extends RedoableOp {

  private int mId;
  private MailItem.Type type;
  private long mDate;

  public DateItem() {
    super(MailboxOperation.DateItem);
  }

  public DateItem(int mailboxId, int itemId, MailItem.Type type, long date) {
    this();
    setMailboxId(mailboxId);
    mId = itemId;
    this.type = type;
    mDate = date;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(mId);
    sb.append(", date=").append(mDate);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeByte(type.toByte());
    out.writeLong(mDate);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    type = MailItem.Type.of(in.readByte());
    mDate = in.readLong();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.setDate(getOperationContext(), mId, type, mDate);
  }
}
