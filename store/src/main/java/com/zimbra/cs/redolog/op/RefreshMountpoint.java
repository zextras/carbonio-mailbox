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

public class RefreshMountpoint extends RedoableOp {

  private int mId; // item id of the mountpoint
  private String mOwnerId; // account id of the remote folder owner
  private int mRemoteId; // item id of the remote folder

  public RefreshMountpoint() {
    super(MailboxOperation.RefreshMountpoint);
    mId = UNKNOWN_ID;
  }

  public RefreshMountpoint(int mailboxId, int mptId, String ownerId, int remoteId) {
    this();
    setMailboxId(mailboxId);
    mId = mptId;
    mOwnerId = ownerId;
    mRemoteId = remoteId;
  }

  @Override
  public void redo() throws Exception {
    int mboxId = getMailboxId();
    Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);
    mailbox.refreshMountpoint(getOperationContext(), mId, mOwnerId, mRemoteId);
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("id=").append(mId);
    sb.append(", owner=").append(mOwnerId).append(", remoteId=").append(mRemoteId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeUTF(mOwnerId);
    out.writeInt(mRemoteId);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    mOwnerId = in.readUTF();
    mRemoteId = in.readInt();
  }
}
