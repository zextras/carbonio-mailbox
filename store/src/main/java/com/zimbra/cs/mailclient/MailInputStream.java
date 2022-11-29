// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;

import com.zimbra.common.util.Log;

/**
 * Input stream for reading line-oriented mail protocol data. Also supports a single character look ahead.
 */
public class MailInputStream extends InputStream {

    private Log log;
    private ByteArrayOutputStream logbuf;

    /** The underlying input stream */
    protected final InputStream in;

    /** Character buffer for reading line data */
    protected final StringBuilder sbuf;

    private int nextByte = -1;

    /**
     * Creates a new {@link MailInputStream} for the specified underlying input stream.
     *
     * @param is the underlying input stream
     */
    public MailInputStream(InputStream is) {
        this.in = is;
        sbuf = new StringBuilder(132);
    }

    public MailInputStream(InputStream is, Log log) {
        this(is);
        this.log = log;
        logbuf = new ByteArrayOutputStream(1024);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (nextByte != -1) {
            b[off++] = (byte) nextByte;
            nextByte = -1;
            len = in.read(b, off, len - 1);
            if (logbuf != null && len > 0) {
                logbuf.write(b, off, len);
            }
            return len != -1 ? len + 1 : 1;
        } else {
            len = in.read(b, off, len);
            if (logbuf != null && len > 0) {
                logbuf.write(b, off, len);
            }
            return len;
        }
    }

    /**
     * Reads the next line of ASCII input data. A input line can be terminated
     * with either CRLF or LF, which is excluded from the returned string.
     * @return the next input line, excluding line terminator
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException {
        sbuf.setLength(0);
        int c = read();
        if (c == -1) return null;
        while (c != '\n' && c != -1) {
            sbuf.append((char) c);
            c = read();
        }
        int len = sbuf.length();
        if (len > 0 && sbuf.charAt(len - 1) == '\r') {
            sbuf.setLength(len - 1);
        }
        return sbuf.toString();
    }

    @Override
    public int read() throws IOException {
        if (nextByte != -1) {
            int b = nextByte;
            nextByte = -1;
            if (logbuf != null && b >= 0) {
                logbuf.write(b);
            }
            return b;
        } else {
            int b = in.read();
            if (logbuf != null && b >= 0) {
                logbuf.write(b);
            }
            return b;
        }
    }

    /**
     * Returns the next byte of data from the input stream without actually
     * reading it. This provides a one byte lookahead.
     *
     * @return the next byte of input data, or <tt>-1</tt> if end of stream
     *         would be reached
     * @throws IOException if an I/O error occurs
     */
    public int peek() throws IOException {
        if (nextByte == -1) {
            nextByte = in.read();
        }
        return nextByte;
    }
    /**
     * Reads an ASCII character from the input stream. An <tt>EOFException</tt>
     * is thrown if the end of the stream is reached.
     *
     * @return the character that has been read
     * @throws EOFException if the end of stream was reached
     * @throws IOException if an I/O error occurs
     */
    public char readChar() throws IOException {
        int c = read();
        if (c == -1) throw new EOFException("Unexpected end of stream");
        return (char) c;
    }

    /**
     * Returns the next ASCII character from the input stream without actually
     * reading it. An <tt>EOFException</tt> is thrown if the end of the stream
     * would be reached.
     *
     * @return the character that would be read
     * @throws EOFException if the end of the stream would be reached
     * @throws IOException if an I/O error occurs
     */
    public char peekChar() throws IOException {
        int c = peek();
        if (c == -1) throw new EOFException("Unexpected end of stream");
        return (char) c;
    }

    /**
     * Returns <tt>true</tt> if the end of the input stream has been reached.
     *
     * @return <tt>true</tt> if at end of stream, <tt>false</tt> if not
     * @throws IOException if an I?O error occurs
     */
    public boolean isEOF() throws IOException {
        return peek() == -1;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        if (nextByte != -1) {
            nextByte = -1;
            return in.skip(n - 1) + 1;
        } else {
            return in.skip(n);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public final void trace() {
        if (logbuf == null || logbuf.size() == 0) {
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace("S: %s", logbuf.toString().trim());
        }
        logbuf.reset();
    }

}
