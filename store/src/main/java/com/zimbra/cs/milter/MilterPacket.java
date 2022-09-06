// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

class MilterPacket {
  private final int len;
  private final byte cmd;
  private final byte[] data;

  MilterPacket(int len, byte cmd, byte[] data) {
    this.len = len;
    this.cmd = cmd;
    this.data = data;
  }

  MilterPacket(byte cmd) {
    this.len = 1;
    this.cmd = cmd;
    this.data = null;
  }

  int getLength() {
    return len;
  }

  byte getCommand() {
    return cmd;
  }

  byte[] getData() {
    return data;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(len);
    sb.append(':');
    sb.append((char) cmd);
    sb.append(':');
    if (data != null) {
      for (byte b : data) {
        if (b > 32 && b < 127) {
          sb.append((char) b);
        } else {
          sb.append("\\");
          sb.append(b & 0xFF); // make unsigned
        }
        sb.append(' ');
      }
    }
    return sb.toString();
  }
}
