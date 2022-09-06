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

public class SetWebOfflineSyncDays extends RedoableOp {

  private int folderId;
  private int days;

  public SetWebOfflineSyncDays() {
    super(MailboxOperation.SetWebOfflineSyncDays);
    folderId = Mailbox.ID_AUTO_INCREMENT;
    days = 0;
  }

  public SetWebOfflineSyncDays(int mailboxId, int folderId, int days) {
    this();
    setMailboxId(mailboxId);
    this.folderId = folderId;
    this.days = days;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("id=").append(folderId);
    sb.append(", webofflinesyncdays=").append(days);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(folderId);
    out.writeInt(days);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    folderId = in.readInt();
    days = in.readInt();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.setFolderWebOfflineSyncDays(getOperationContext(), folderId, days);
  }
}
