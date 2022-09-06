// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.cs.volume.VolumeServiceException;
import java.io.IOException;

public final class DeleteVolume extends RedoableOp {

  private short mId;

  public DeleteVolume() {
    super(MailboxOperation.DeleteVolume);
  }

  public DeleteVolume(short id) {
    this();
    mId = id;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("id=").append(mId);
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeShort(mId);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mId = in.readShort();
  }

  @Override
  public void redo() throws Exception {
    VolumeManager mgr = VolumeManager.getInstance();
    try {
      mgr.getVolume(mId); // make sure it exists
      mgr.delete(mId, getUnloggedReplay());
    } catch (VolumeServiceException e) {
      if (e.getCode() != VolumeServiceException.NO_SUCH_VOLUME) {
        throw e;
      }
    }
  }
}
