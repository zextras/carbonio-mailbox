// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class SaveChat extends CreateChat {

  private int mImapId; // new IMAP id for this message

  public SaveChat() {
    mOperation = MailboxOperation.SaveChat;
  }

  public SaveChat(
      int mailboxId,
      int chatId,
      String digest,
      int msgSize,
      int folderId,
      int flags,
      String[] tags) {
    super(mailboxId, digest, msgSize, folderId, flags, tags);
    mOperation = MailboxOperation.SaveChat;
    setMessageId(chatId);
  }

  public int getImapId() {
    return mImapId;
  }

  public void setImapId(int imapId) {
    mImapId = imapId;
  }

  @Override
  protected String getPrintableData() {
    return super.getPrintableData() + ",imap=" + mImapId;
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mImapId);
    super.serializeData(out);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mImapId = in.readInt();
    super.deserializeData(in);
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());

    ParsedMessage pm =
        new ParsedMessage(getMessageBody(), getTimestamp(), mbox.attachmentsIndexingEnabled());
    mbox.updateChat(getOperationContext(), pm, getMessageId());
  }
}
