// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import com.zimbra.cs.server.NioUtil;
import java.nio.ByteBuffer;
import javax.security.sasl.SaslException;
import org.apache.mina.core.buffer.IoBuffer;

public class SaslOutputBuffer {
  private final int mMaxSize;
  private ByteBuffer mBuffer;

  private static final int MINSIZE = 512;

  public SaslOutputBuffer(int maxSize) {
    this(Math.min(MINSIZE, maxSize), maxSize);
  }

  public SaslOutputBuffer(int minSize, int maxSize) {
    if (minSize > maxSize) {
      throw new IllegalArgumentException("minSize > maxSize");
    }
    mBuffer = ByteBuffer.allocate(minSize);
    mMaxSize = maxSize;
  }

  public void put(IoBuffer buf) {
    put(buf.buf());
  }

  public void put(ByteBuffer bb) {
    if (isFull()) return;
    if (bb.remaining() > mBuffer.remaining()) {
      int minSize = Math.min(bb.remaining(), mMaxSize);
      mBuffer = NioUtil.expand(mBuffer, minSize, mMaxSize);
    }
    int len = Math.min(mBuffer.remaining(), bb.remaining());
    int pos = mBuffer.position();
    bb.get(mBuffer.array(), pos, len);
    mBuffer.position(pos + len);
  }

  public void put(byte b) {
    if (isFull()) return;
    if (!mBuffer.hasRemaining()) {
      mBuffer = NioUtil.expand(mBuffer, 1, mMaxSize);
    }
    mBuffer.put(b);
  }

  public int size() {
    return mBuffer.position();
  }

  public boolean isFull() {
    return mBuffer.position() >= mMaxSize;
  }

  public byte[] wrap(SaslSecurityLayer security) throws SaslException {
    return security.wrap(mBuffer.array(), 0, mBuffer.position());
  }

  public void clear() {
    mBuffer.clear();
  }
}
