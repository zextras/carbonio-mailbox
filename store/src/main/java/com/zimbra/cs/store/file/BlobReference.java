// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import com.zimbra.znative.IO.FileInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class BlobReference implements Serializable {

  private static final long serialVersionUID = -1132877177535802600L;

  private long id;
  private int mailboxId;
  private short volumeId;
  private int itemId;
  private int revision;
  private String digest;
  private boolean processed;
  private FileInfo fileInfo;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getMailboxId() {
    return mailboxId;
  }

  public void setMailboxId(int mailboxId) {
    this.mailboxId = mailboxId;
  }

  public short getVolumeId() {
    return volumeId;
  }

  public void setVolumeId(short volumeId) {
    this.volumeId = volumeId;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(String digest) {
    this.digest = digest;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }

  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  private final void readObject(ObjectInputStream in) throws java.io.IOException {
    throw new IOException("Cannot be deserialized");
  }
}
