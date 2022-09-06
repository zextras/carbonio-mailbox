// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 12. 14.
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.Note;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class RepositionNote extends RedoableOp {

  private int mId;
  private Note.Rectangle mBounds;

  public RepositionNote() {
    super(MailboxOperation.RepositionNote);
    mId = UNKNOWN_ID;
  }

  public RepositionNote(int mailboxId, int id, Note.Rectangle bounds) {
    this();
    setMailboxId(mailboxId);
    mId = id;
    mBounds = bounds;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=");
    sb.append(mId);
    if (mBounds != null) sb.append(", bounds=(").append(mBounds).append(")");
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    out.writeInt(mBounds.x);
    out.writeInt(mBounds.y);
    out.writeInt(mBounds.width);
    out.writeInt(mBounds.height);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    int x = in.readInt();
    int y = in.readInt();
    int w = in.readInt();
    int h = in.readInt();
    mBounds = new Note.Rectangle(x, y, w, h);
  }

  @Override
  public void redo() throws Exception {
    Mailbox mailbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
    mailbox.repositionNote(getOperationContext(), mId, mBounds);
  }
}
