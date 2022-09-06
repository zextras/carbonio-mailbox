// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class CreateLink extends RedoableOp {

  private int mId;
  private String mUuid;
  private int mFolderId;
  private String mName;
  private String mOwnerId;
  private int mRemoteId;

  public CreateLink() {
    super(MailboxOperation.CreateMountpoint);
    mId = UNKNOWN_ID;
  }

  public CreateLink(int mailboxId, int folderId, String name, String ownerId, int remoteId) {
    this();
    setMailboxId(mailboxId);
    mId = UNKNOWN_ID;
    mFolderId = folderId;
    mName = name != null ? name : "";
    mOwnerId = ownerId;
    mRemoteId = remoteId;
  }

  public int getId() {
    return mId;
  }

  public String getUuid() {
    return mUuid;
  }

  public void setIdAndUuid(int id, String uuid) {
    mId = id;
    mUuid = uuid;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("id=").append(mId);
    sb.append(", uuid=").append(mUuid);
    sb.append(", name=").append(mName).append(", folder=").append(mFolderId);
    sb.append(", owner=").append(mOwnerId).append(", remote=").append(mRemoteId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeInt(mId);
    if (getVersion().atLeast(1, 37)) {
      out.writeUTF(mUuid);
    }
    out.writeUTF(mName);
    out.writeUTF(mOwnerId);
    out.writeInt(mRemoteId);
    out.writeInt(mFolderId);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readInt();
    if (getVersion().atLeast(1, 37)) {
      mUuid = in.readUTF();
    }
    mName = in.readUTF();
    mOwnerId = in.readUTF();
    mRemoteId = in.readInt();
    mFolderId = in.readInt();
  }

  @Override
  public void redo() throws Exception {
    int mboxId = getMailboxId();
    Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);

    try {
      mailbox.createLink(getOperationContext(), mFolderId, mName, mOwnerId, mRemoteId);
    } catch (MailServiceException e) {
      if (e.getCode() == MailServiceException.ALREADY_EXISTS) {
        if (mLog.isInfoEnabled()) {
          mLog.info("Link " + mId + " already exists in mailbox " + mboxId);
        }
      } else {
        throw e;
      }
    }
  }
}
