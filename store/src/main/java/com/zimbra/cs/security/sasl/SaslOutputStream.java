// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslServer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SaslOutputStream extends OutputStream {
    private final DataOutputStream mOutputStream;
    private final SaslSecurityLayer mSecurityLayer;

    // Inlined from deleted SaslOutputBuffer
    private ByteBuffer mBuffer;
    private final int mMaxSize;
    private static final int MINSIZE = 512;

    private static final boolean DEBUG = false;

    public SaslOutputStream(OutputStream os, SaslServer server) {
        this(os, SaslSecurityLayer.getInstance(server));
    }

    public SaslOutputStream(OutputStream os, SaslClient client) {
        this(os, SaslSecurityLayer.getInstance(client));
    }

    public SaslOutputStream(OutputStream os, SaslSecurityLayer securityLayer) {
        mOutputStream = new DataOutputStream(os);
        mSecurityLayer = securityLayer;
        mMaxSize = securityLayer.getMaxSendSize();
        mBuffer = ByteBuffer.allocate(Math.min(MINSIZE, mMaxSize));
    }

    @Override public void write(byte[] b, int off, int len) throws IOException {
        debug("write: enter len = %d", len);
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int count = 0; count < len; ) {
            int n = writeBytes(b, off, len - count);
            count += n;
            off += n;
            debug("write: loop n = %d, count = %d", n, count);
        }
        debug("write: exit");
    }

    @Override public void write(int b) throws IOException {
        debug("write: enter b = %d", b);
        ensureBuffer();
        bufferPut((byte) b);
    }

    private int writeBytes(byte[] b, int off, int len) throws IOException {
        ensureBuffer();
        ByteBuffer bb = ByteBuffer.wrap(b, off, len);
        bufferPut(bb);
        return bb.position();
    }

    private void ensureBuffer() throws IOException {
        if (bufferIsFull()) flushBuffer();
    }

    private void flushBuffer() throws IOException {
        byte[] b = mSecurityLayer.wrap(mBuffer.array(), 0, mBuffer.position());
        mOutputStream.writeInt(b.length);
        mOutputStream.write(b);
        mBuffer.clear();
    }

    @Override public void flush() throws IOException {
        if (DEBUG) debug("flushBuffer: size = %d", mBuffer.position());
        if (mBuffer.position() > 0) flushBuffer();
        mOutputStream.flush();
    }

    @Override public void close() throws IOException {
        flush();
        mOutputStream.close();
    }

    // --- inlined SaslOutputBuffer helpers ---

    private void bufferPut(ByteBuffer bb) {
        if (bufferIsFull()) return;
        if (bb.remaining() > mBuffer.remaining()) {
            int minSize = Math.min(bb.remaining(), mMaxSize);
            mBuffer = expand(mBuffer, minSize, mMaxSize);
        }
        int len = Math.min(mBuffer.remaining(), bb.remaining());
        int pos = mBuffer.position();
        bb.get(mBuffer.array(), pos, len);
        mBuffer.position(pos + len);
    }

    private void bufferPut(byte b) {
        if (bufferIsFull()) return;
        if (!mBuffer.hasRemaining()) {
            mBuffer = expand(mBuffer, 1, mMaxSize);
        }
        mBuffer.put(b);
    }

    private boolean bufferIsFull() {
        return mBuffer.position() >= mMaxSize;
    }

    private static ByteBuffer expand(ByteBuffer buf, int needed, int maxSize) {
        int newSize = Math.min(Math.max(buf.capacity() * 2, buf.position() + needed), maxSize);
        ByteBuffer expanded = ByteBuffer.allocate(newSize);
        buf.flip();
        expanded.put(buf);
        return expanded;
    }

    private static void debug(String format, Object... args) {
        if (DEBUG) {
            System.out.printf("[DEBUG] " + format + "\n", args);
        }
    }
}
