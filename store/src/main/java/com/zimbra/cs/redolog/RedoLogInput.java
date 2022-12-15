// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.zimbra.common.util.ByteUtil;

/**
 * This class is equivalent to java.io.DataInputStream except that readUTF()
 * method doesn't have 64KB limit thanks to using a different serialization
 * format. (thus incompatible with DataInputStream)  This class is not derived
 * from DataInputStream and does not implement DataInput interface, to prevent
 * using either of those in redo log operation classes.
 * 
 * @author jhahm
 */
public class RedoLogInput {
    private DataInput mIN;
    private String mPath;

    public RedoLogInput(InputStream is) {
        mIN = new DataInputStream(is);
    }

    public RedoLogInput(RandomAccessFile raf, String path) {
        mPath = path;
        mIN = raf;
    }

    /**
     * Returns the path to the redo log file, or <tt>null</tt> if this object
     * reads from an <tt>InputStream</tt>. 
     */
    public String getPath() {
        return mPath;
    }

    /**
     * Returns the current offset in this file, or <tt>-1</tt> if this object
     * reads from an <tt>InputStream</tt>.
     */
    public long getFilePointer()
    throws IOException {
        if (mIN instanceof RandomAccessFile) {
            RandomAccessFile file = (RandomAccessFile) mIN;
            return file.getFilePointer();
        }
        return -1;
    }

    public int skipBytes(int n) throws IOException { return mIN.skipBytes(n); }
    public void readFully(byte[] b) throws IOException { mIN.readFully(b); }
    public void readFully(byte[] b, int off, int len) throws IOException { mIN.readFully(b, off, len); }
    public boolean readBoolean() throws IOException { return mIN.readBoolean(); }
    public byte readByte() throws IOException { return mIN.readByte(); }
    public int readUnsignedByte() throws IOException { return mIN.readUnsignedByte(); }
    public short readShort() throws IOException { return mIN.readShort(); }
    public int readUnsignedShort() throws IOException { return mIN.readUnsignedShort(); }
    public int readInt() throws IOException { return mIN.readInt(); }
    public long readLong() throws IOException { return mIN.readLong(); }
    public double readDouble() throws IOException { return mIN.readDouble(); }

    public String readUTF() throws IOException {
        return ByteUtil.readUTF8(mIN);
    }

    public String[] readUTFArray() throws IOException {
        int count = readInt();
        if (count < 0) {
            return null;
        }

        String[] v = new String[count];
        for (int i = 0; i < count; i++) {
            v[i] = readUTF();
        }
        return v;
    }

    // methods of DataInput that shouldn't be used in redo logging
    // not implemented on purpose

    //public String readLine() throws IOException { return mIN.readLine(); }
    //public char readChar(int v) throws IOException { return mIN.readChar(); }
    //public float readFloat() throws IOException { return mIN.readFloat(); }
}
