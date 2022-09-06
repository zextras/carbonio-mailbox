// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.common.mailbox.Color;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

/**
 * @since Sep 23, 2005
 */
public class CreateMountpoint extends RedoableOp {

  private int mId;
  private String mUuid;
  private int mFolderId;
  private String mName;
  private String mOwnerId;
  private int mRemoteId;
  private String mRemoteUuid;
  private MailItem.Type defaultView;
  private int mFlags;
  private long mColor;
  private boolean mReminderEnabled;

  public CreateMountpoint() {
    super(MailboxOperation.CreateMountpoint);
    mId = UNKNOWN_ID;
  }

  public CreateMountpoint(
      int mailboxId,
      int folderId,
      String name,
      String ownerId,
      int remoteId,
      String remoteUuid,
      MailItem.Type view,
      int flags,
      Color color,
      boolean reminderEnabled) {
    this();
    setMailboxId(mailboxId);
    mId = UNKNOWN_ID;
    mFolderId = folderId;
    mName = name != null ? name : "";
    mOwnerId = ownerId;
    mRemoteId = remoteId;
    mRemoteUuid = remoteUuid;
    defaultView = view;
    mFlags = flags;
    mColor = color.getValue();
    mReminderEnabled = reminderEnabled;
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
    sb.append(", owner=")
        .append(mOwnerId)
        .append(", remoteId=")
        .append(mRemoteId)
        .append(", remoteUuid=")
        .append(mRemoteUuid);
    sb.append(", view=")
        .append(defaultView)
        .append(", flags=")
        .append(mFlags)
        .append(", color=")
        .append(mColor);
    sb.append(", reminder=").append(mReminderEnabled);
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
    if (getVersion().atLeast(1, 38)) {
      out.writeUTF(mRemoteUuid);
    }
    out.writeInt(mFolderId);
    out.writeByte(defaultView.toByte());
    out.writeInt(mFlags);
    out.writeLong(mColor); // mColor from byte to long in Version 1.27
    out.writeBoolean(mReminderEnabled); // since version 1.33
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
    if (getVersion().atLeast(1, 38)) {
      mRemoteUuid = in.readUTF();
    }
    mFolderId = in.readInt();
    defaultView = MailItem.Type.of(in.readByte());
    mFlags = in.readInt();
    if (getVersion().atLeast(1, 27)) {
      mColor = in.readLong();
    } else {
      mColor = in.readByte();
    }
    if (getVersion().atLeast(1, 33)) {
      mReminderEnabled = in.readBoolean();
    }
  }

  @Override
  public void redo() throws Exception {
    int mboxId = getMailboxId();
    Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);

    try {
      mailbox.createMountpoint(
          getOperationContext(),
          mFolderId,
          mName,
          mOwnerId,
          mRemoteId,
          mRemoteUuid,
          defaultView,
          mFlags,
          Color.fromMetadata(mColor),
          mReminderEnabled);
    } catch (MailServiceException e) {
      if (e.getCode() == MailServiceException.ALREADY_EXISTS) {
        if (mLog.isInfoEnabled()) {
          mLog.info("Mount " + mId + " already exists in mailbox " + mboxId);
        }
      } else {
        throw e;
      }
    }
  }
}
