// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.util.ByteUtil.PositionInputStream;
import com.zimbra.common.util.ByteUtil.SegmentInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.mail.internet.SharedInputStream;

public class ZSharedFileInputStream extends PositionInputStream implements SharedInputStream {
  private final File file;
  private final long soff, eoff;

  public ZSharedFileInputStream(String path) {
    this(new File(path));
  }

  public ZSharedFileInputStream(File file) {
    super(null);
    this.file = file;
    this.soff = 0;
    this.eoff = file.length();
  }

  public ZSharedFileInputStream(File file, long soff, long eoff) {
    super(null);
    assert soff <= eoff;
    this.file = file;
    this.soff = soff;
    this.eoff = eoff;
  }

  private void buffer() throws IOException {
    if (in == null) {
      if (soff == 0 && eoff == file.length()) {
        in = new BufferedInputStream(new FileInputStream(file), 4096);
      } else {
        // not sure what the better way to do this is...
        in =
            new BufferedInputStream(
                SegmentInputStream.create(new FileInputStream(file), soff, eoff));
        //                InputStream rafis = Channels.newInputStream(new RandomAccessFile(file,
        // "r").getChannel().position(soff));
        //                in = new BufferedInputStream(new SegmentInputStream(rafis, eoff - soff),
        // 4096);
      }
    }
  }

  @Override
  public int read() throws IOException {
    buffer();
    return super.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    buffer();
    return super.read(b, off, len);
  }

  @Override
  public synchronized boolean markSupported() {
    try {
      buffer();
      return super.markSupported();
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public synchronized void mark(int readlimit) {
    try {
      buffer();
      super.mark(readlimit);
    } catch (IOException e) {
      // well, we must be at position 0, so what the hell...
    }
  }

  @Override
  public synchronized void reset() throws IOException {
    buffer();
    super.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    buffer();
    return super.skip(n);
  }

  @Override
  public int available() throws IOException {
    return (int) (eoff - soff);
  }

  @Override
  public ZSharedFileInputStream newStream(long start, long end) {
    return new ZSharedFileInputStream(
        file, Math.min(eoff, soff + start), end >= 0 ? Math.min(eoff, soff + end) : eoff);
  }

  @VisibleForTesting
  boolean isBuffered() {
    return in != null;
  }
}
