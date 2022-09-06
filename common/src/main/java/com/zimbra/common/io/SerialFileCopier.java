// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.io;

import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.znative.IO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class SerialFileCopier implements FileCopier {

  private static final int MAX_COPY_BUFSIZE = 1024 * 1024; // 1MB

  private boolean mUseNIO;
  private int mCopyBufSizeOIO;
  private boolean mIgnoreMissingSource;

  SerialFileCopier(boolean useNIO, int copyBufSizeOIO) {
    ZimbraLog.io.debug(
        "Creating SerialFileCopier: "
            + "useNIO = "
            + useNIO
            + ", copyBufSizeOIO = "
            + copyBufSizeOIO);

    mUseNIO = useNIO;
    mCopyBufSizeOIO =
        copyBufSizeOIO > 0 ? copyBufSizeOIO : FileCopierOptions.DEFAULT_OIO_COPY_BUFFER_SIZE;
    if (mCopyBufSizeOIO > MAX_COPY_BUFSIZE) {
      ZimbraLog.io.warn(
          "OIO copy buffer size "
              + mCopyBufSizeOIO
              + " is too big; limiting to "
              + MAX_COPY_BUFSIZE);
      mCopyBufSizeOIO = MAX_COPY_BUFSIZE;
    }
  }

  public boolean isAsync() {
    return false;
  }

  public void start() {
    ZimbraLog.io.info("SerialFileCopier is starting");
    // do nothing
  }

  public void shutdown() {
    ZimbraLog.io.info("SerialFileCopier is shut down");
    // do nothing
  }

  public synchronized void setIgnoreMissingSource(boolean ignore) {
    mIgnoreMissingSource = ignore;
  }

  private synchronized boolean ignoreMissingSource() {
    return mIgnoreMissingSource;
  }

  public void copy(File src, File dest, FileCopierCallback cb, Object cbarg) throws IOException {
    FileUtil.ensureDirExists(dest.getParentFile());
    try {
      if (mUseNIO) {
        FileUtil.copy(src, dest);
      } else {
        byte[] buf = new byte[mCopyBufSizeOIO];
        FileUtil.copyOIO(src, dest, buf);
      }
    } catch (FileNotFoundException e) {
      if (!ignoreMissingSource()) throw e;
    }
  }

  public void copyReadOnly(File src, File dest, FileCopierCallback cb, Object cbarg)
      throws IOException {
    copy(src, dest, cb, cbarg);
    if (dest.exists()) dest.setReadOnly();
  }

  public void link(File real, File link, FileCopierCallback cb, Object cbarg) throws IOException {
    FileUtil.ensureDirExists(link.getParentFile());
    try {
      IO.link(real.getAbsolutePath(), link.getAbsolutePath());
    } catch (FileNotFoundException e) {
      if (!ignoreMissingSource()) throw e;
    }
  }

  public void move(File oldPath, File newPath, FileCopierCallback cb, Object cbarg)
      throws IOException {
    FileUtil.ensureDirExists(newPath.getParentFile());
    oldPath.renameTo(newPath);
  }

  public void delete(File file, FileCopierCallback cb, Object cbarg) {
    file.delete();
  }
}
