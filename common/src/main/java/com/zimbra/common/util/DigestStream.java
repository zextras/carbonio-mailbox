// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.security.MessageDigest;

public class DigestStream extends BufferStream {
  private MessageDigest messageDigest;

  public DigestStream() {
    this(0);
  }

  public DigestStream(long sizeHint) {
    this(sizeHint, Integer.MAX_VALUE);
  }

  public DigestStream(long sizeHint, int maxBuffer) {
    this(sizeHint, maxBuffer, Long.MAX_VALUE);
  }

  public DigestStream(long sizeHint, int maxBuffer, long maxSize) {
    super(sizeHint, maxBuffer, maxSize);
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (Exception e) {
      throw new RuntimeException("Unable to initialize " + DigestStream.class.getSimpleName(), e);
    }
  }

  public String getDigest() {
    return ByteUtil.encodeFSSafeBase64(messageDigest.digest());
  }

  public void write(int data) {
    super.write(data);
    messageDigest.update((byte) data);
  }

  public void write(byte data[], int off, int len) {
    super.write(data, off, len);
    messageDigest.update(data, off, len);
  }
}
