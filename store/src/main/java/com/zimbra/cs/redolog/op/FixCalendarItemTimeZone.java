// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class FixCalendarItemTimeZone extends RedoableOp {

  private int mId;
  private long mAfter;
  private String mCountry; // ISO-3166 two-letter country code, or null for world

  public FixCalendarItemTimeZone() {
    super(MailboxOperation.FixCalendarItemTimeZone);
  }

  public FixCalendarItemTimeZone(int mailboxId, int itemId, long after, String country) {
    this();
    setMailboxId(mailboxId);
    mId = itemId;
    mAfter = after;
    mCountry = country;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("id=");
    sb.append(mId);
    sb.append(", after=").append(mAfter);
    sb.append(", country=").append(mCountry);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeLong(mAfter);
    out.writeUTF(mCountry);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    mAfter = in.readLong();
    mCountry = in.readUTF();
  }

  @Override
  public void redo() throws Exception {
    // do nothing; this op has been deprecated
  }
}
