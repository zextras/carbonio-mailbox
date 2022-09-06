// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.volume.VolumeManager;
import java.io.IOException;

public class SetCurrentVolume extends RedoableOp {

  private short mType;
  private short mId;

  public SetCurrentVolume() {
    super(MailboxOperation.SetCurrentVolume);
  }

  public SetCurrentVolume(short type, short id) {
    this();
    mType = type;
    mId = id;
  }

  @Override
  protected String getPrintableData() {
    StringBuilder sb = new StringBuilder("type=").append(mType);
    sb.append(", id=").append(mId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeShort(mType);
    out.writeShort(mId);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mType = in.readShort();
    mId = in.readShort();
  }

  @Override
  public void redo() throws Exception {
    VolumeManager.getInstance().setCurrentVolume(mType, mId, getUnloggedReplay());
  }
}
