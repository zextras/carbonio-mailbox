// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

public final class NioUtil {
    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte DOT = '.';

    public static final int INITIAL_CAPACITY = 32;

    /**
     * Ensure that specified buffer has at least enough capacity to accommodate 'minSize' additional bytes, but not more
     * than 'maxSize' additional bytes. If 'maxSize' is 0, then there is no limit.
     *
     * @param bb the ByteBuffer to expand, or null to create a new one
     * @param minSize minimum additional capacity for resulting byte buffer
     * @param maxSize maximum additional capacity, or -1 for no maximum
     * @return the resulting, possible expanded, ByteBuffer
     */
    public static ByteBuffer expand(ByteBuffer bb, int minSize, int maxSize) {
        if (maxSize != -1 && maxSize < minSize) {
            throw new IllegalArgumentException("maxSize < minSize");
        }
        if (bb == null) {
            int size = Math.max(minSize, INITIAL_CAPACITY);
            if (maxSize != -1 && size > maxSize) size = maxSize;
            return ByteBuffer.allocate(size);
        }
        if (bb.remaining() >= minSize) return bb;
        int capacity = Math.max((bb.capacity() * 3) / 2 + 1,
                                bb.position() + minSize);
        if (maxSize != -1) {
            capacity = Math.max(capacity, bb.position() + maxSize);
        }
        ByteBuffer tmp = ByteBuffer.allocate(capacity);
        bb.flip();
        return tmp.put(bb);
    }

    public static ByteBuffer expand(ByteBuffer bb, int minSize) {
        return expand(bb, minSize, -1);
    }

    public static String toAsciiString(ByteBuffer bb) {
        int len = bb.remaining();
        char[] cs = new char[len];
        for (int i = 0; i < len; i++) {
            cs[i] = (char) (bb.get(i) & 0xff);
        }
        return new String(cs);
    }

    public static ByteBuffer toAsciiBytes(String s) {
        return put(ByteBuffer.allocate(s.length()), s);
    }

    public static ByteBuffer put(ByteBuffer bb, String s) {
        bb = expand(bb, s.length());
        for (int i = 0; i < s.length(); i++) {
            bb.put(i, (byte) s.charAt(i));
        }
        return bb;
    }

    public static byte[] getBytes(ByteBuffer bb) {
        if (bb.hasArray() && bb.arrayOffset() == 0 && bb.position() == 0) {
            byte[] b = bb.array();
            if (b.length == bb.limit()) return b;
        }
        byte[] b = new byte[bb.limit() - bb.position()];
        bb.duplicate().get(b);
        return b;
    }

    public static IoBuffer toIoBuffer(ByteBuffer bb) {
        return IoBuffer.wrap(bb);
    }

    public static String toHexString(ByteBuffer bb) {
        return appendHex(new StringBuilder(), bb).toString();
    }

    public static StringBuilder appendHex(StringBuilder sb, ByteBuffer bb) {
        int limit = bb.limit();
        for (int i = bb.position(); i < limit; i++) {
            int c = bb.get(i) & 0xff;
            sb.append(Character.forDigit(c >> 4, 16));
            sb.append(Character.forDigit(c & 0xf, 16));
            if (i < limit - 1) sb.append(' ');
        }
        return sb;
    }
}
