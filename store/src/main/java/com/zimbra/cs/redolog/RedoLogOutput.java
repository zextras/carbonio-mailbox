// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import com.zimbra.common.util.ByteUtil;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class is equivalent to java.io.DataOutputStream except that writeUTF() method doesn't have
 * 64KB limit thanks to using a different serialization format. (thus incompatible with
 * DataOutputStream) This class is not derived from DataOutputStream and does not implement
 * DataOutput interface, to prevent using either of those in redo log operation classes.
 *
 * @author jhahm
 */
public class RedoLogOutput {
  private DataOutput mOUT;

  public RedoLogOutput(OutputStream os) {
    mOUT = new DataOutputStream(os);
  }

  public RedoLogOutput(RandomAccessFile raf) {
    mOUT = raf;
  }

  public void write(byte[] b) throws IOException {
    mOUT.write(b);
  }

  public void writeBoolean(boolean v) throws IOException {
    mOUT.writeBoolean(v);
  }

  public void writeByte(byte v) throws IOException {
    mOUT.writeByte(v);
  }

  public void writeShort(short v) throws IOException {
    mOUT.writeShort(v);
  }

  public void writeInt(int v) throws IOException {
    mOUT.writeInt(v);
  }

  public void writeLong(long v) throws IOException {
    mOUT.writeLong(v);
  }

  public void writeDouble(double v) throws IOException {
    mOUT.writeDouble(v);
  }

  public void writeUTF(String v) throws IOException {
    ByteUtil.writeUTF8(mOUT, v);
  }

  public void writeUTFArray(String[] v) throws IOException {
    if (v == null) {
      writeInt(-1);
    } else {
      writeInt(v.length);
      for (String s : v) {
        writeUTF(s);
      }
    }
  }

  // methods of DataOutput that shouldn't be used in redo logging
  // not implemented on purpose

  // public void write(byte[] b, int off, int len) throws IOException { mOUT.write(b, off, len); }
  // public void write(int b) throws IOException { mOUT.write(b); }
  // public void writeBytes(String v) throws IOException { mOUT.writeBytes(v); }
  // public void writeChar(int v) throws IOException { mOUT.writeChar(v); }
  // public void writeChars(String v) throws IOException { mOUT.writeChars(v); }
  // public void writeFloat(float v) throws IOException { mOUT.writeFloat(v); }
}
