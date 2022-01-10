// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.util;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer buf;

    public ByteBufferOutputStream(int size) {
        buf = ByteBuffer.allocate(size);
    }

    public ByteBufferOutputStream() {
        this(32);
    }

    public void write(int b) throws IOException {
        ensureCapacity(1);
        buf.put((byte) b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (off < 0 || off > b.length || len < 0 ||
            off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(len);
        buf.put(b, off, len);
    }

    private void ensureCapacity(int count) throws IOException {
        if (buf.remaining() < count) {
            int size = Math.max(buf.capacity() * 2, buf.capacity() + count);
            if (size < 0) {
                throw new IOException("buffer limit exceeded");
            }
            ByteBuffer tmp = ByteBuffer.allocate(size);
            buf.flip();
            tmp.put(buf);
            buf = tmp;
        }
    }

    public ByteBuffer getByteBuffer() {
        return buf;
    }
}
