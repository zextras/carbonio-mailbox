// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.DataSource;

import com.zimbra.common.mime.MimePart.PartSource;

public class MimeParserInputStream extends FilterInputStream {
    private MimeParser parser;
    private MimePart.PartSource psource;
    private MimeHeaderBlock headers;

    public MimeParserInputStream(InputStream in) {
        super(in);
        parser = new MimeParser();
    }

    public MimeParserInputStream(InputStream in, MimeHeaderBlock hblock) {
        super(in);
        parser = new MimeParser(hblock);
        headers = hblock;
    }

    @Override public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            parser.handleByte((byte) b);
        }
        return b;
    }

    @Override public int read(byte[] b, int off, int len) throws IOException {
        int amt = super.read(b, off, len);
        if (amt != -1) {
            parser.handleBytes(b, off, amt);
        }
        return amt;
    }

    @Override public long skip(long n) throws IOException {
        long remaining = n;
        int max = (int) Math.min(n, 32768), read = 0;
        final byte buffer[] = new byte[max];

        while (remaining > 0 && (read = read(buffer, 0, (int) Math.min(remaining, max))) != -1) {
            remaining -= read;
        }
        return n - remaining;
    }

    @Override public void close() throws IOException {
        super.close();
        parser.endParse();
    }


    public MimeParserInputStream setSource(byte[] content) {
        psource = content == null ? null : new PartSource(content);
        return this;
    }

    public MimeParserInputStream setSource(File file) {
        psource = file == null || !file.exists() ? null : new PartSource(file);
        return this;
    }

    public MimeParserInputStream setSource(DataSource ds) {
        psource = ds == null ? null : new PartSource(ds);
        return this;
    }

    public MimeParserInputStream setSource(MimePart.InputStreamSource iss) {
        psource = iss == null ? null : new PartSource(iss);
        return this;
    }

    public MimePart getPart() {
        MimePart mp = parser.getPart().attachSource(psource);
        if (mp instanceof MimeBodyPart && headers != null && !headers.containsHeader("Content-Transfer-Encoding")) {
            MimeBodyPart mbp = (MimeBodyPart) mp;
            try {
                mbp.setTransferEncoding(mbp.pickEncoding());
            } catch (IOException ioe) {
                mbp.setTransferEncoding(ContentTransferEncoding.BASE64);
            }
        }
        return mp;
    }

    <T extends MimeMessage> T insertBodyPart(T mm) {
        mm.setBodyPart(getPart());
        mm.recordEndpoint(parser.getPosition(), parser.getLineNumber());
        mm.attachSource(psource);
        return mm;
    }

    public MimeMessage getMessage(Properties props) {
        MimeMessage mm = new MimeMessage(getPart(), props);
        mm.recordEndpoint(parser.getPosition(), parser.getLineNumber());
        mm.attachSource(psource);
        return mm;
    }
}
