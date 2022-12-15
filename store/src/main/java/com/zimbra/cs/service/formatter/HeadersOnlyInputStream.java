// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.zimbra.common.util.ByteUtil;

/**
 * Returns data from the encapsulated stream until <tt>CRLFCRLF</tt> is reached.
 * The trailing <tt>CRLFCRLF</tt> is read from the wrapped stream, but is not
 * returned by the <tt>read()</tt> methods in <tt>HeadersOnlyInputStream</tt>.
 */
public class HeadersOnlyInputStream extends InputStream {

    private InputStream mIn;
    private boolean mEOF = false;
    
    public HeadersOnlyInputStream(InputStream in) {
        if (!in.markSupported())
            in = new BufferedInputStream(in);
        mIn = in;
    }
    
    @Override public int read() throws IOException {
        if (mEOF)
            return -1;

        int c = mIn.read();
        if (c == '\r') {
            mIn.mark(4);
            if (mIn.read() != '\n' ||
                mIn.read() != '\r' ||
                mIn.read() != '\n') {
                mIn.reset();
            } else {
                mEOF = true;
                return -1;
            }
        }
        return c;
    }

    @Override public void close() throws IOException {
        mIn.close();
    }

    @Override public synchronized void mark(int readlimit) {
        mIn.mark(readlimit);
    }

    @Override public boolean markSupported() {
        return mIn.markSupported();
    }

    @Override public synchronized void reset() throws IOException {
        mIn.reset();
    }

    public static byte[] getHeaders(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
        ByteUtil.copy(new HeadersOnlyInputStream(is), true, buf, false);
        return buf.toByteArray();
    }
}
