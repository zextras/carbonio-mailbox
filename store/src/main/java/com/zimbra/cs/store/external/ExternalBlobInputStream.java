// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.ibm.icu.impl.Assert;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.cs.store.StoreManager;
import java.io.File;
import java.io.IOException;

public class ExternalBlobInputStream extends BlobInputStream {

  protected String locator;
  protected Mailbox mbox;

  public ExternalBlobInputStream(Blob blob) throws IOException {
    super(blob);
    if (blob instanceof ExternalBlob) {
      locator = ((ExternalBlob) blob).getLocator();
      mbox = ((ExternalBlob) blob).getMbox();
    } else {
      Assert.fail("ExternalBlobInputStream constructor Blob instance not ExternalBlob");
    }
  }

  private ExternalBlobInputStream(
      File file, long rawSize, Long start, Long end, BlobInputStream parent) throws IOException {
    super(file, rawSize, start, end, parent);
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
  protected void setMailboxLocator(BlobInputStream parent) {
    if (parent instanceof ExternalBlobInputStream) {
      this.locator = ((ExternalBlobInputStream) parent).getLocator();
      this.mbox = ((ExternalBlobInputStream) parent).getMbox();
    } else {
      Assert.fail("Parent of ExternalBlobInputStream must always be ExternalBlobInputStream");
    }
  }

  @Override
  protected BlobInputStream initializeSubStream(
      File file, long rawSize, Long start, Long end, BlobInputStream parent) throws IOException {
    return new ExternalBlobInputStream(file, rawSize, start, end, parent);
  }

  @Override
  protected File getRootFile() throws IOException {
    File file = super.getRootFile();
    if (file != null && file.exists()) {
      return file;
    } else {
      ZimbraLog.store.debug("blob file no longer on disk, fetching from remote store");
      ExternalStoreManager sm = (ExternalStoreManager) StoreManager.getInstance();
      Blob blob = sm.getLocalBlob(mbox, locator, false);
      return blob.getFile();
    }
  }
}
