// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.common.util.FileCache;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import java.io.File;

/** Wrapper around Blob for constructor visibility and to allow construction from FileCache.Item */
public class ExternalBlob extends Blob {

  protected String locator;
  protected Mailbox mbox;

  protected ExternalBlob(File file) {
    super(file);
  }

  public ExternalBlob(File file, long rawSize, String digest) {
    super(file, rawSize, digest);
  }

  ExternalBlob(FileCache.Item cachedFile) {
    super(cachedFile.file, cachedFile.file.length(), cachedFile.digest);
  }

  public String getLocator() {
    return locator;
  }

  public void setLocator(String locator) {
    this.locator = locator;
  }

  public Mailbox getMbox() {
    return mbox;
  }

  public void setMbox(Mailbox mbox) {
    this.mbox = mbox;
  }

  @Override
  public File getFile() {
    return super.getFile();
  }
}
