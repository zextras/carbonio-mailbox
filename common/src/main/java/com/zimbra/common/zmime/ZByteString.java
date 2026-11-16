// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import java.nio.charset.Charset;

import com.zimbra.common.zmime.ZMimeUtility.ByteBuilder;

public class ZByteString {

    private byte[] bytes;
    private int offset;
    private int length;
    private Charset charset;

    public ZByteString(byte[] bytes, int offset, int length, Charset charset) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.charset = charset;
    }

    public ZByteString(ByteBuilder builder) {
        this.bytes = builder.toByteArray();
        this.offset = 0;
        this.length = this.bytes.length;
        this.charset = builder.getCharset();
    }

    public boolean canMerge (ZByteString byteString) {
        return this.charset.equals(byteString.charset);
    }

    public ZByteString merge (ZByteString byteString) throws IllegalArgumentException {
        if (this.canMerge(byteString)) {
            int newLength = this.length + byteString.length;
            byte[] newBytes = new byte[newLength];
            System.arraycopy(this.bytes, this.offset, newBytes, 0, this.length);
            System.arraycopy(byteString.bytes, byteString.offset, newBytes, this.length, byteString.length);
            return new ZByteString(newBytes, 0, newLength, this.charset);
        } else {
            throw new IllegalArgumentException("merged charsets must match");
        }
    }

    @Override
    public String toString () {
        return new String(this.bytes, this.offset, this.length, this.charset);
    }
}

