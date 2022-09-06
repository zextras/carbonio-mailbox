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

public class PurgeRevision extends RedoableOp {

  protected int mId;
  protected int mRev;
  protected boolean mIncludeOlderRevisions;

  public PurgeRevision() {
    super(MailboxOperation.PurgeRevision);
  }

  public PurgeRevision(int mailboxId, int id, int rev, boolean includeOlderRevisions) {
    this();
    setMailboxId(mailboxId);
    mId = id;
    mRev = rev;
    mIncludeOlderRevisions = includeOlderRevisions;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(mId);
    sb.append(", rev=").append(mRev);
    sb.append(", includeOlderRevisions=").append(mIncludeOlderRevisions);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeInt(mRev);
    out.writeBoolean(mIncludeOlderRevisions);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    mRev = in.readInt();
    mIncludeOlderRevisions = in.readBoolean();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.purgeRevision(getOperationContext(), mId, mRev, mIncludeOlderRevisions);
  }
}
