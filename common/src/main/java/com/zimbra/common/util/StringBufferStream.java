// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.IOException;
import java.nio.charset.Charset;

public class StringBufferStream extends BufferStream implements Appendable {
    String cset;
    StringBuilder sbuf;
    static final int DEFAULT_SIZE_HINT = 512;

    public StringBufferStream() { this(null); }

    public StringBufferStream(long sizeHint) {
        this(null, sizeHint);
    }

    public StringBufferStream(long sizeHint, int maxBuffer) {
        this(null, sizeHint, maxBuffer);
    }

    public StringBufferStream(long sizeHint, int maxBuffer, long maxSize) {
        this(null, sizeHint, maxBuffer, maxSize);
    }

    public StringBufferStream(String cset) { this(cset, 0); }

    public StringBufferStream(String cset, long sizeHint) {
        this(cset, sizeHint, Integer.MAX_VALUE);
    }

    public StringBufferStream(String cset, long sizeHint, int maxBuffer) {
        this(cset, sizeHint, maxBuffer, Long.MAX_VALUE);
    }

    public StringBufferStream(String cset, long sizeHint, int maxBuffer,
        long maxSize) {
        super(sizeHint, maxBuffer, maxSize);
        this.cset = cset == null ? Charset.defaultCharset().toString() : cset;
        sbuf = new StringBuilder((int)Math.min(sizeHint == 0 ?
            DEFAULT_SIZE_HINT : sizeHint, 2 * 1024));
    }

    public Appendable append(char c) throws IOException {
        flush(1);
        sbuf.append(c);
        return this;
    }

    public Appendable append(CharSequence cs) throws IOException {
        flush(cs.length());
        if (cs.length() > sbuf.capacity() - sbuf.length())
            write(cs.toString().getBytes(cset));
        else
            sbuf.append(cs);
        return this;
    }

    public Appendable append(CharSequence cs, int start, int end) throws
        IOException {
        int len = end - start;
        
        flush(len);
        if (len > sbuf.capacity() - sbuf.length())
            write(cs.subSequence(start, end).toString().getBytes(cset));
        else
            sbuf.append(cs, start, end);
        return this;
    }

    private void flush(int len) throws IOException {
        if (sbuf.capacity() - sbuf.length() < len && sbuf.length() > 0) {
            write(sbuf.toString().getBytes(cset));
            sbuf.setLength(0);
        }
    }
    
    public void sync() throws IOException {
        flush(Integer.MAX_VALUE);
        super.sync();
    }
    
    public String toString() {
        try {
            return super.toString(cset);
        } catch (Exception e) {
            return new String(getBuffer());
        }
    }
    
    public static void main(String[] args) throws IOException {
        StringBufferStream sbs = new StringBufferStream(12);
        
        sbs.append("start ");
        sbs.append(new StringBuilder("-middle-"), 1, 7);
        sbs.append(" end");
        System.out.println(sbs.toString());
    }

}
