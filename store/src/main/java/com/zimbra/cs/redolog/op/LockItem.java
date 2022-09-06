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

public class LockItem extends RedoableOp {

  protected int id;
  protected MailItem.Type type;
  protected String accountId;

  public LockItem() {
    super(MailboxOperation.LockItem);
  }

  public LockItem(int mailboxId, int id, MailItem.Type type, String accountId) {
    this();
    setMailboxId(mailboxId);
    this.id = id;
    this.type = type;
    this.accountId = accountId;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(id);
    sb.append(", type=").append(type);
    sb.append(", accountId=").append(accountId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeByte(type.toByte());
    out.writeUTF(accountId);
    out.writeInt(id);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    type = MailItem.Type.of(in.readByte());
    accountId = in.readUTF();
    id = in.readInt();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.lock(getOperationContext(), id, type, accountId);
  }
}
