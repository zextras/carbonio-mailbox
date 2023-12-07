// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.google.common.collect.Lists;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class SetRetentionPolicy extends RedoableOp {

  private int itemId = -1;
  private RetentionPolicy retentionPolicy;
  private MailItem.Type type;

  public SetRetentionPolicy() {
    super(MailboxOperation.SetRetentionPolicy);
  }

  public SetRetentionPolicy(int mailboxId, MailItem.Type type, int itemId, RetentionPolicy rp) {
    this();
    setMailboxId(mailboxId);
    this.type = type;
    this.itemId = itemId;
    retentionPolicy = rp;
  }

  @Override
  protected String getPrintableData() {
    return "retentionPolicy=" + retentionPolicy;
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    List<Policy> purgePolicy = retentionPolicy.getPurgePolicy();

    out.writeByte(type.toByte());
    out.writeInt(itemId);
    out.writeInt(purgePolicy.size());
    for (Policy policy : purgePolicy) {
      out.writeUTF(policy.getId());
      out.writeUTF(policy.getLifetime());
    }
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    type = MailItem.Type.of(in.readByte());
    if (type != MailItem.Type.FOLDER && type != MailItem.Type.TAG) {
      throw new IOException("Unexpected item type: " + type);
    }
    itemId = in.readInt();
    int size = in.readInt();

    List<Policy> purge = readPolicyList(in, size);
    retentionPolicy = new RetentionPolicy(purge);
  }

  /** Reads a list of {@code RetentionPolicy} objects from input. */
  private List<Policy> readPolicyList(RedoLogInput in, int size) throws IOException {
    List<Policy> list = Lists.newArrayList();
    for (int i = 1; i <= size; i++) {
      String id = in.readUTF();
      String lifetime = in.readUTF();
      Policy p;
      if (id != null) {
        p = Policy.newSystemPolicy(id);
      } else {
        p = Policy.newUserPolicy(lifetime);
      }
      list.add(p);
    }
    return list;
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    if (type == MailItem.Type.FOLDER) {
      mbox.setRetentionPolicy(getOperationContext(), itemId, MailItem.Type.FOLDER, retentionPolicy);
    } else {
      mbox.setRetentionPolicy(getOperationContext(), itemId, MailItem.Type.TAG, retentionPolicy);
    }
  }

  //////////////// Unit test methods /////////////////

  int getItemId() {
    return itemId;
  }

  public RetentionPolicy getRetentionPolicy() {
    return retentionPolicy;
  }

  byte[] testSerialize() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializeData(new RedoLogOutput(out));
    return out.toByteArray();
  }

  void testDeserialize(byte[] data) throws IOException {
    deserializeData(new RedoLogInput(new ByteArrayInputStream(data)));
  }
}
