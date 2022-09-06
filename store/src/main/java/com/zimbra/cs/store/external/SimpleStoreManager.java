// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 * Example implementation of ExternalStoreManager which writes to a flat directory structure This is
 * intended for illustration purposes only; it should *never* be used in a production environment
 */
public class SimpleStoreManager extends ExternalStoreManager {

  String directory = null;

  @Override
  public void startup() throws IOException, ServiceException {
    super.startup();
    ZimbraLog.store.info(
        "Using SimpleStoreManager. If you are seeing this in production you have done something"
            + " WRONG!");
    File blobDirectory = new File(LC.zimbra_tmp_directory.value(), "simplestore/blobs");
    directory = blobDirectory.getAbsolutePath();
    FileUtil.mkdirs(blobDirectory);
  }

  @Override
  public void shutdown() {
    super.shutdown();
  }

  @VisibleForTesting
  public String dirName(Mailbox mbox) {
    return directory + "/" + mbox.getAccountId();
  }

  private File getNewFile(Mailbox mbox) throws IOException {
    String baseName = dirName(mbox);
    FileUtil.mkdirs(new File(baseName));
    baseName += "/zimbrablob";
    String name = baseName;
    synchronized (this) {
      int count = 1;
      File file = new File(name + ".msg");
      while (file.exists()) {
        name = baseName + "_" + count++;
        file = new File(name + ".msg");
      }
      if (file.createNewFile()) {
        ZimbraLog.store.debug("writing to new file %s", file.getName());
        return file;
      } else {
        throw new IOException("unable to create new file");
      }
    }
  }

  @Override
  public String writeStreamToStore(InputStream in, long actualSize, Mailbox mbox)
      throws IOException {
    File destFile = getNewFile(mbox);
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
  public List<String> getAllBlobPaths(Mailbox mbox) throws IOException {
    File dir = new File(dirName(mbox));
    if (dir.exists()) {
      File[] files = dir.listFiles((FileFilter) FileFileFilter.FILE);
      List<String> locators = new ArrayList<String>();
      for (File file : files) {
        locators.add(file.getCanonicalPath());
      }
      return locators;
    } else {
      return new ArrayList<String>();
    }
  }

  @Override
  public boolean supports(StoreFeature feature) {
    if (feature == StoreFeature.CENTRALIZED) {
      return false;
    } else {
      return super.supports(feature);
    }
  }
}
