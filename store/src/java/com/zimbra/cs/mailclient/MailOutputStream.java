// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.zimbra.common.util.Log;

/**
 * An output stream for writing line-oriented mail protocol data.
 */
public class MailOutputStream extends OutputStream {
    private Log log;
    private ByteArrayOutputStream logbuf;
    private boolean privacy = false;

    /** The underlying output stream */
    protected final OutputStream out;

    /**
     * Creates a new {@link MailOutputStream} for the specified underlying output stream.
     *
     * @param out the underlying output stream
     */
    public MailOutputStream(OutputStream out) {
        this.out = out;
    }

    public MailOutputStream(OutputStream out, Log log) {
        this(out);
        this.log = log;
        logbuf = new ByteArrayOutputStream(1024);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (logbuf != null) {
            if (privacy) {
                for (int i = 0; i < len; i++) {
                    logbuf.write('*');
                }
            } else {
                logbuf.write(b, off, len);
            }
        }
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        if (logbuf != null) {
            if (privacy) {
                logbuf.write('*');
            } else {
                logbuf.write(b);
            }
        }
        out.write(b);
    }

    /**
     * Writes the specified ASCII string to the output stream.
     *
     * @param s the string that is to be written
     * @throws IOException if an I/O error occurs
     */
    public void write(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            write(s.charAt(i));
        }
    }

    /**
     * Writes a line of ASCII characters to the output stream. The line will
     * be terminated CRLF.
     *
     * @param s the line that is to be written
     * @throws IOException if an I/O error occurs
     */
    public void writeLine(String s) throws IOException {
        write(s);
        newLine();
    }

    /**
     * Writes a terminating CRLF to the output stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void newLine() throws IOException {
        write('\r');
        write('\n');
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Mask the trace log.
     *
     * @param value true to start masking, false to stop masking
     */
    public final void setPrivacy(boolean value) {
        privacy = value;
    }

    public final void trace() {
        if (logbuf == null || logbuf.size() == 0) {
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace("C: %s", logbuf.toString().trim());
        }
        logbuf.reset();
    }

}
