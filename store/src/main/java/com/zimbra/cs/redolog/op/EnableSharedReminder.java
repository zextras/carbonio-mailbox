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

public class EnableSharedReminder extends RedoableOp {

  private int mountpointId;
  private boolean enabled;

  public EnableSharedReminder() {
    super(MailboxOperation.EnableSharedReminder);
    mountpointId = UNKNOWN_ID;
    enabled = false;
  }

  public EnableSharedReminder(int mailboxId, int mountpointId, boolean enabled) {
    this();
    setMailboxId(mailboxId);
    this.mountpointId = mountpointId;
    this.enabled = enabled;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("mountpoint=").append(mountpointId);
    sb.append(", reminderEnabled=").append(enabled);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mountpointId);
    out.writeBoolean(enabled);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mountpointId = in.readInt();
    enabled = in.readBoolean();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.enableSharedReminder(getOperationContext(), mountpointId, enabled);
  }
}
