// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.FileUtil;
import com.zimbra.cs.mailbox.Mailbox;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple ExternalStoreManager implementation that is intended for use in storing transient blobs.
 *
 * <p>This StoreManager is used by the {@link com.zimbra.cs.imap.ImapDaemon} while constructing
 * blobs for an APPEND operation. The blobs are deleted when the APPEND is finalized.
 */
public class ImapTransientStoreManager extends ExternalStoreManager {

  protected File baseDirectory;

  @Override
  public void startup() throws IOException, ServiceException {
    startup(LC.imapd_tmp_directory.value());
  }

  public void startup(String baseDirectoryPath) throws IOException, ServiceException {
    super.startup();
    baseDirectory = new File(baseDirectoryPath);
    FileUtil.mkdirs(baseDirectory);
  }

  @Override
  public void shutdown() {
    super.shutdown();
  }

  @Override
  public String writeStreamToStore(InputStream in, long actualSize, Mailbox mbox)
      throws IOException, ServiceException {
    File destFile = createBlobFile(mbox);
    FileUtil.copy(in, false, destFile);
    return destFile.getCanonicalPath();
  }

  @Override
  public InputStream readStreamFromStore(String locator, Mailbox mbox) throws IOException {
    return new FileInputStream(locator);
  }

  @Override
  public boolean deleteFromStore(String locator, Mailbox mbox) throws IOException {
    File deleteFile = new File(locator);
    return deleteFile.delete();
  }

  @Override
  public boolean supports(StoreFeature feature) {
    return feature == StoreFeature.CENTRALIZED ? false : super.supports(feature);
  }

  private File createBlobFile(Mailbox mbox) throws IOException {
    synchronized (this) {
      return File.createTempFile(mbox.getAccountId(), ".msg", baseDirectory);
    }
  }
}
