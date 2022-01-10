// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import java.nio.charset.Charset;
import java.util.List;

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

    public static String makeString(List<ZByteString> byteStrings) {
        StringBuilder builder = new StringBuilder();
        ZByteString lastByteString = null;
        for (ZByteString bStr : byteStrings) {
            if (lastByteString != null) {
                if (lastByteString.canMerge(bStr)) {
                    lastByteString = lastByteString.merge(bStr);
                }
                else {
                    builder.append(lastByteString.toString());
                    lastByteString = bStr;
                }
            }
            else {
                lastByteString = bStr;
            }
        }
        if (lastByteString != null) {
            builder.append(lastByteString.toString());
        }
        return builder.toString();
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

