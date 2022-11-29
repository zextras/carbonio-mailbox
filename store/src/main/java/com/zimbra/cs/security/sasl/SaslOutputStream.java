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
    private final SaslOutputBuffer mBuffer;

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
        mBuffer = new SaslOutputBuffer(securityLayer.getMaxSendSize());
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
        mBuffer.put((byte) b);
    }
    
    private int writeBytes(byte[] b, int off, int len) throws IOException {
        ensureBuffer();
        ByteBuffer bb = ByteBuffer.wrap(b, off, len);
        mBuffer.put(bb);
        return bb.position();
    }

    private void ensureBuffer() throws IOException {
        if (mBuffer.isFull()) flushBuffer();
    }

    private void flushBuffer() throws IOException {
        byte[] b = mBuffer.wrap(mSecurityLayer);
        mOutputStream.writeInt(b.length);
        mOutputStream.write(b);
        mBuffer.clear();
    }

    @Override public void flush() throws IOException {
        if (DEBUG) debug("flushBuffer: size = %d", mBuffer.size());
        if (mBuffer.size() > 0) flushBuffer();
        mOutputStream.flush();
    }

    @Override public void close() throws IOException {
        flush();
        mOutputStream.close();
    }

    private static void debug(String format, Object... args) {
        if (DEBUG) {
            System.out.printf("[DEBUG] " + format + "\n", args);
        }
    }
}
