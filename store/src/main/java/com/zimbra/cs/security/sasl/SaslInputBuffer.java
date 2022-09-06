// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import java.nio.ByteBuffer;
import javax.security.sasl.SaslException;
import org.apache.mina.core.buffer.IoBuffer;

public class SaslInputBuffer {
  private final int mMaxSize;
  private final ByteBuffer mLenBuffer;
  private ByteBuffer mDataBuffer;

  public SaslInputBuffer(int maxSize) {
    mMaxSize = maxSize;
    mLenBuffer = ByteBuffer.allocate(4);
  }

  public void put(IoBuffer buf) throws SaslException {
    put(buf.buf());
  }

  public void put(ByteBuffer bb) throws SaslException {
    if (isComplete()) return;
    if (mLenBuffer.hasRemaining() && !readLength(bb)) return;
    int len = Math.min(mDataBuffer.remaining(), bb.remaining());
    int pos = mDataBuffer.position();
    bb.get(mDataBuffer.array(), pos, len);
    mDataBuffer.position(pos + len);
  }

  public boolean isComplete() {
    return mDataBuffer != null && !mDataBuffer.hasRemaining();
  }

  public int getLength() {
    return mDataBuffer != null ? mDataBuffer.limit() : -1;
  }

  public int getRemaining() {
    return mDataBuffer != null ? mDataBuffer.remaining() : -1;
  }

  public byte[] unwrap(SaslSecurityLayer securityLayer) throws SaslException {
    if (!isComplete()) {
      throw new IllegalStateException("input not complete");
    }
    return securityLayer.unwrap(mDataBuffer.array(), 0, mDataBuffer.position());
  }

  public void clear() {
    mLenBuffer.clear();
    if (mDataBuffer != null) mDataBuffer.clear();
  }

  private boolean readLength(ByteBuffer bb) throws SaslException {
    // Copy rest of length bytes
    while (mLenBuffer.hasRemaining()) {
      if (!bb.hasRemaining()) return false;
      mLenBuffer.put(bb.get());
    }
    int len = mLenBuffer.getInt(0);
    if (len < 0 || len > mMaxSize) {
      throw new SaslException(
          "Invalid receive buffer size '" + len + "' (max size = " + mMaxSize + ")");
    }
    if (mDataBuffer == null || mDataBuffer.capacity() < len) {
      mDataBuffer = ByteBuffer.allocate(len);
    }
    mDataBuffer.limit(len);
    return true;
  }
}
