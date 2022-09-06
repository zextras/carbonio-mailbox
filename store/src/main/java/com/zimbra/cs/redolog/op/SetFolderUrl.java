// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 12, 2005
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class SetFolderUrl extends RedoableOp {

  private int mFolderId;
  private String mURL;

  public SetFolderUrl() {
    super(MailboxOperation.SetFolderUrl);
    mFolderId = Mailbox.ID_AUTO_INCREMENT;
    mURL = "";
  }

  public SetFolderUrl(int mailboxId, int folderId, String url) {
    this();
    setMailboxId(mailboxId);
    mFolderId = folderId;
    mURL = url == null ? "" : url;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(mFolderId);
    sb.append(", url=").append(mURL);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mFolderId);
    out.writeUTF(mURL);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mFolderId = in.readInt();
    mURL = in.readUTF();
  }

  @Override
  public void redo() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mbox.setFolderUrl(getOperationContext(), mFolderId, mURL);
  }
}
