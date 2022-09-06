// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jul 24, 2005
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetImapUid extends RedoableOp {

  private Map<Integer, Integer> mImapUids = new HashMap<Integer, Integer>();

  public SetImapUid() {
    super(MailboxOperation.SetImapUid);
  }

  public SetImapUid(int mailboxId, List<Integer> msgIds) {
    this();
    setMailboxId(mailboxId);
    for (int id : msgIds) mImapUids.put(id, UNKNOWN_ID);
  }

  public int getImapUid(int msgId) {
    Integer imapUid = mImapUids.get(msgId);
    int uid = imapUid == null ? Mailbox.ID_AUTO_INCREMENT : imapUid;
    return uid == UNKNOWN_ID ? Mailbox.ID_AUTO_INCREMENT : uid;
  }

  public void setImapUid(int msgId, int imapId) {
    mImapUids.put(msgId, imapId);
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer, Integer> entry : mImapUids.entrySet())
      sb.append(sb.length() == 0 ? "" : ", ")
          .append(entry.getKey())
          .append('=')
          .append(entry.getValue());
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mImapUids.size());
    for (Map.Entry<Integer, Integer> entry : mImapUids.entrySet()) {
      out.writeInt(entry.getKey());
      out.writeInt(entry.getValue());
    }
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    int count = in.readInt();
    for (int i = 0; i < count; i++) {
      int msgId = in.readInt();
      mImapUids.put(msgId, in.readInt());
    }
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.resetImapUid(getOperationContext(), new ArrayList<Integer>(mImapUids.keySet()));
  }
}
