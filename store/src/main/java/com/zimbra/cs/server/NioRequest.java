// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import java.io.IOException;
import java.nio.ByteBuffer;

/** Represents request that has been received by a MINA-based server. */
public interface NioRequest {
  /**
   * Parses specified bytes for the request. Any remaining bytes are left in the specified buffer.
   *
   * @param bb the byte buffer containing the request bytes
   * @throws IllegalArgumentException if the request could not be parsed
   * @throws IOException if an I/O error occurs
   */
  void parse(ByteBuffer bb) throws IOException;

  /**
   * Returns true if the request is complete and no more bytes are required.
   *
   * @return true if the request is complete, otherwise false
   */
  boolean isComplete();
}
