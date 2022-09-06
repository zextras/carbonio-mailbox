// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @since 2004. 10. 26.
 * @author jhahm
 */
public class TcpServerInputStream extends BufferedInputStream {

  StringBuilder buffer;
  protected static final int CR = 13;
  protected static final int LF = 10;

  public TcpServerInputStream(InputStream in) {
    super(in);
    buffer = new StringBuilder(128);
  }

  public TcpServerInputStream(InputStream in, int size) {
    super(in, size);
    buffer = new StringBuilder(128);
  }

  /**
   * Reads a line from the stream. A line is terminated with either CRLF or bare LF. (This is
   * different from the behavior of BufferedReader.readLine() which considers a bare CR as line
   * terminator.)
   *
   * @return A String containing the contents of the line, not including any line-termination
   *     characters, or null if the end of the stream has been reached
   * @throws IOException
   */
  public String readLine() throws IOException {
    buffer.delete(0, buffer.length());
    while (true) {
      int ch = read();
      if (ch == -1) {
        return null;
      } else if (ch == CR) {
        continue;
      } else if (ch == LF) {
        return buffer.toString();
      }
      buffer.append((char) ch);
    }
  }
}
