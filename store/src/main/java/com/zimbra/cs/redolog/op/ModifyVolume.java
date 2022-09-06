// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.google.common.base.MoreObjects;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.IOException;

public final class ModifyVolume extends RedoableOp {

  private short id;
  private short type;
  private String name;
  private String rootPath;

  private short mboxGroupBits;
  private short mboxBits;
  private short fileGroupBits;
  private short fileBits;

  private boolean compressBlobs;
  private long compressionThreshold;

  public ModifyVolume() {
    super(MailboxOperation.ModifyVolume);
  }

  public ModifyVolume(Volume volume) {
    this();
    id = volume.getId();
    type = volume.getType();
    name = volume.getName();
    rootPath = volume.getRootPath();

    mboxGroupBits = volume.getMboxGroupBits();
    mboxBits = volume.getMboxBits();
    fileGroupBits = volume.getFileGroupBits();
    fileBits = volume.getFileBits();

    compressBlobs = volume.isCompressBlobs();
    compressionThreshold = volume.getCompressionThreshold();
  }

  @Override
  protected String getPrintableData() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("type", type)
        .add("name", name)
        .add("path", rootPath)
        .add("mboxGroupBits", mboxGroupBits)
        .add("mboxBits", mboxBits)
        .add("fileGroupBits", fileGroupBits)
        .add("fileBits", fileBits)
        .add("compressBlobs", compressBlobs)
        .add("compressionThrehold", compressionThreshold)
        .toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeShort(id);
    out.writeShort(type);
    out.writeUTF(name);
    out.writeUTF(rootPath);
    out.writeShort(mboxGroupBits);
    out.writeShort(mboxBits);
    out.writeShort(fileGroupBits);
    out.writeShort(fileBits);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    id = in.readShort();
    type = in.readShort();
    name = in.readUTF();
    rootPath = in.readUTF();
    mboxGroupBits = in.readShort();
    mboxBits = in.readShort();
    fileGroupBits = in.readShort();
    fileBits = in.readShort();
  }

  @Override
  public void redo() throws Exception {
    VolumeManager mgr = VolumeManager.getInstance();
    mgr.getVolume(id); // make sure it exists
    Volume vol =
        Volume.builder()
            .setId(id)
            .setType(type)
            .setName(name)
            .setPath(rootPath, false)
            .setMboxGroupBits(mboxGroupBits)
            .setMboxBit(mboxBits)
            .setFileGroupBits(fileGroupBits)
            .setFileBits(fileBits)
            .setCompressBlobs(compressBlobs)
            .setCompressionThreshold(compressionThreshold)
            .build();
    mgr.update(vol, getUnloggedReplay());
  }
}
