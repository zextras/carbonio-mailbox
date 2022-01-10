// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @since 2004. 10. 26.
 * @author jhahm
 */
public class TcpServerOutputStream extends BufferedOutputStream {

    protected static final byte[] CRLF = { (byte) 13, (byte) 10 };

    public TcpServerOutputStream(OutputStream out) {
        super(out);
    }

    public TcpServerOutputStream(OutputStream out, int size) {
        super(out, size);
    }

    public void writeLine() throws IOException {
        write(CRLF, 0, CRLF.length);
    }

    public void writeLine(String str) throws IOException {
        byte[] data = str.getBytes();
        write(data, 0, data.length);
        writeLine();
    }
}
