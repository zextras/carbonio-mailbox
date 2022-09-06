// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a HTTPClient <tt>GetMethod</tt> and automatically releases resources when the stream is
 * closed.
 */
public class GetMethodInputStream extends InputStream {

  private InputStream mIn;

  public GetMethodInputStream(InputStream in) throws IOException {
    mIn = in;
  }

  @Override
  public int read() throws IOException {
    return mIn.read();
  }

  @Override
  public int available() throws IOException {
    return mIn.available();
  }

  @Override
  public void close() throws IOException {
    mIn.close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    mIn.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return mIn.markSupported();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return mIn.read(b, off, len);
  }

  @Override
  public int read(byte[] b) throws IOException {
    return mIn.read(b);
  }

  @Override
  public synchronized void reset() throws IOException {
    mIn.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return mIn.skip(n);
  }
}
