// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.cs.mailclient.util.Ascii;
import java.io.IOException;
import java.io.OutputStream;

/** IMAP quoted string data type. */
public final class Quoted extends ImapData {
  private final String string;

  public Quoted(String s) {
    string = s;
  }

  public Type getType() {
    return Type.QUOTED;
  }

  public int getSize() {
    return string.length();
  }

  public byte[] getBytes() {
    return Ascii.getBytes(string);
  }

  public void write(OutputStream os) throws IOException {
    os.write('"');
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      switch (c) {
        case '\\':
        case '"':
          os.write('\\');
        default:
          os.write(c);
      }
    }
    os.write('"');
  }

  public int hashCode() {
    return string.hashCode();
  }

  public boolean equals(Object obj) {
    return this == obj
        || obj != null && obj.getClass() == Quoted.class && string.equals(((Quoted) obj).string);
  }

  public String toString() {
    return string;
  }
}
