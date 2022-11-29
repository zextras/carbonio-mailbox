// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.util.StringUtil;

public class MimeBodyPart extends MimePart {

    private ContentTransferEncoding encoding, targetEncoding;

    /** Maximum number of octets in a line before we force a content transfer
     *  encoding.  Many MTAs will wrap lines over 998 octets. */
    private static final int MAX_LINE_OCTETS = 900;

    public MimeBodyPart(ContentType ctype) {
        super(ctype != null ? ctype : new ContentType(ContentType.TEXT_PLAIN));
        encoding = targetEncoding = ContentTransferEncoding.BINARY;
    }

    MimeBodyPart(ContentType ctype, MimePart parent, long start, long body, MimeHeaderBlock headers) {
        super(ctype, parent, start, body, headers);
        encoding = targetEncoding = ContentTransferEncoding.forString(getMimeHeader("Content-Transfer-Encoding"));
    }

    MimeBodyPart(MimeBodyPart mbp) {
        super(mbp);
        encoding = mbp.encoding;
        targetEncoding = mbp.targetEncoding;
    }

    @Override
    protected MimeBodyPart clone() {
        return new MimeBodyPart(this);
    }

    @Override
    void removeChild(MimePart mp) {}

    @Override
    ContentType updateContentType(ContentType ctype) {
        if (ctype != null && (ctype.getPrimaryType().equals("multipart") || ctype.getContentType().equals(ContentType.MESSAGE_RFC822))) {
            throw new UnsupportedOperationException("cannot change a part into a multipart or message: " + ctype);
        }
        return super.updateContentType(ctype == null ? new ContentType(ContentType.TEXT_PLAIN) : ctype);
    }

    public ContentTransferEncoding getTransferEncoding() {
        return targetEncoding;
    }

    public MimeBodyPart setTransferEncoding(ContentTransferEncoding cte) {
        // our markDirty() will take care of updating the target encoding
        setMimeHeader("Content-Transfer-Encoding", cte == null ? null : cte.toString());
        return this;
    }

    @Override
    public long getSize() throws IOException {
        long size = super.getSize();
        if (size == -1) {
            size = recordSize(ByteUtil.countBytes(getRawContentStream()));
        }
        return size;
    }

    @Override
    public InputStream getRawContentStream() throws IOException {
        InputStream stream = super.getRawContentStream();
        if (encoding.normalize() != targetEncoding.normalize()) {
            // decode the raw version if necessary
            if (encoding == ContentTransferEncoding.BASE64) {
                stream = new ContentTransferEncoding.Base64DecoderStream(stream);
            } else if (encoding == ContentTransferEncoding.QUOTED_PRINTABLE) {
                stream = new ContentTransferEncoding.QuotedPrintableDecoderStream(stream);
            }
            // encode to the target encoding if necessary
            if (targetEncoding == ContentTransferEncoding.BASE64) {
                stream = new ContentTransferEncoding.Base64EncoderStream(stream);
            } else if (targetEncoding == ContentTransferEncoding.QUOTED_PRINTABLE) {
                stream = new ContentTransferEncoding.QuotedPrintableEncoderStream(stream, getContentType());
            }
        }
        return stream;
    }

    @Override
    public byte[] getRawContent() throws IOException {
        if (encoding.normalize() == targetEncoding.normalize()) {
            return super.getRawContent();
        } else {
            return ByteUtil.getContent(getRawContentStream(), -1);
        }
    }

    @Override
    public InputStream getContentStream() throws IOException {
        InputStream raw = super.getRawContentStream();
        if (encoding == ContentTransferEncoding.BASE64) {
            return new ContentTransferEncoding.Base64DecoderStream(raw);
        } else if (encoding == ContentTransferEncoding.QUOTED_PRINTABLE) {
            return new ContentTransferEncoding.QuotedPrintableDecoderStream(raw);
        } else {
            return raw;
        }
    }

    @Override
    public byte[] getContent() throws IOException {
        // certain encodings mean that the decoded content is the same as the raw content
        if (encoding.normalize() == ContentTransferEncoding.BINARY) {
            return super.getRawContent();
        } else {
            return ByteUtil.getContent(getContentStream(), (int) (getSize() * (encoding == ContentTransferEncoding.BASE64 ? 0.75 : 1.0)));
        }
    }

    public Reader getTextReader() throws IOException {
        InputStream is = getContentStream();

        String charset = getContentType().getParameter("charset");
        if (!StringUtil.isNullOrEmpty(charset)) {
            try {
                return new InputStreamReader(is, CharsetUtil.normalizeCharset(charset));
            } catch (UnsupportedEncodingException e) {
            }
        }

        // if we're here, either there was no explicit charset or it was invalid, so try the default charset
        String defaultCharset = getDefaultCharset();
        if (!StringUtil.isNullOrEmpty(defaultCharset)) {
            try {
                return new InputStreamReader(is, CharsetUtil.normalizeCharset(defaultCharset));
            } catch (UnsupportedEncodingException e) {
            }
        }

        // if we're here, the default charset was also either unspecified or unavailable, so go with the JVM's default charset
        return new InputStreamReader(is);
    }

    public String getText() throws IOException {
        StringBuilder buffer = new StringBuilder();
        Reader reader = getTextReader();
        try {
            char[] cbuff = new char[8192];
            int num;
            while ((num = reader.read(cbuff, 0, cbuff.length)) != -1) {
                buffer.append(cbuff, 0, num);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    /** Changes the <tt>Content-Type</tt> of the part to <tt>text/plain</tt>
     *  and sets the part's content to the given text using the default
     *  charset. */
    public MimeBodyPart setText(String text) throws IOException {
        return setText(text, null, null, null);
    }

    public MimeBodyPart setText(String text, String charset, String subtype, ContentTransferEncoding cte) throws IOException {
        // default the subtype and charset appropriately
        ContentType ctype = getContentType();
        ctype.setContentType("text/" + (subtype == null || subtype.trim().isEmpty() ? ctype.getSubType() : subtype));

        String cset = charset;
        if (cset == null || cset.trim().isEmpty()) {
            cset = ctype.getParameter("charset");
        }
        if (cset == null || cset.trim().isEmpty()) {
            cset = getDefaultCharset();
        }
        if (cset == null || cset.trim().isEmpty()) {
            cset = "utf-8";
        }
        ctype.setParameter("charset", cset);

        setContent((text == null ? "" : text).getBytes(cset), cte);
        setContentType(ctype);
        return this;
    }

    public MimeBodyPart setContent(byte[] content) throws IOException {
        return setContent(content, null);
    }

    public MimeBodyPart setContent(byte[] content, ContentTransferEncoding cte) throws IOException {
        return setContent(content == null ? null : new PartSource(content), cte);
    }

    public MimeBodyPart setContent(File file) throws IOException {
        return setContent(file, null);
    }

    public MimeBodyPart setContent(File file, ContentTransferEncoding cte) throws IOException {
        return setContent(file == null || !file.exists() ? null : new PartSource(file), cte);
    }

    public MimeBodyPart setContent(DataSource ds) throws IOException {
        return setContent(ds, null);
    }

    public MimeBodyPart setContent(DataSource ds, ContentTransferEncoding cte) throws IOException {
        return setContent(ds == null ? null : new PartSource(ds), cte);
    }

    public MimeBodyPart setContent(InputStreamSource iss) throws IOException {
        return setContent(iss, null);
    }

    public MimeBodyPart setContent(InputStreamSource iss, ContentTransferEncoding cte) throws IOException {
        return setContent(iss == null ? null : new PartSource(iss), cte);
    }

    private MimeBodyPart setContent(PartSource psource, ContentTransferEncoding cte) throws IOException {
        super.setContent(psource);
        encoding = ContentTransferEncoding.BINARY;
        // cascade: set header, which marks dirty, which sets the target encoding
        setTransferEncoding(cte == null ? pickEncoding() : cte);
        return this;
    }

    ContentTransferEncoding pickEncoding() throws IOException {
        boolean sevenbit = true;
        int qpencodeable = 0, toolong = 0, length = 0, column = 0;

        InputStream is = getRawContentStream();
        if (is != null) {
            try {
                is = is instanceof ByteArrayInputStream || is instanceof BufferedInputStream ? is : new BufferedInputStream(is);
                for (int octet = is.read(); octet != -1; octet = is.read()) {
                    if (octet >= 0x7F || (octet < 0x20 && octet != '\t' && octet != '\r' && octet != '\n')) {
                        // this octet must be encoded if we choose quoted-printable (RFC2045 6.7)
                        qpencodeable++;
                        // all of these octets except for non-NUL control chars rule out "7bit" (RFC2045 2.7)
                        if (sevenbit && (octet == 0x00 || octet >= 0x7F)) {
                            sevenbit = false;
                        }
                    }
                    if (octet == '\n') {
                        if (column > MAX_LINE_OCTETS) {
                            toolong++;
                        }
                        column = 0;
                    } else {
                        column++;
                    }
                    length++;
                }
            } finally {
                ByteUtil.closeStream(is);
            }
        }
        if (column > MAX_LINE_OCTETS) {
            toolong++;
        }

        if (sevenbit && toolong == 0) {
            return ContentTransferEncoding.SEVEN_BIT;
        } else if (qpencodeable < length / 4) {
            return ContentTransferEncoding.QUOTED_PRINTABLE;
        } else {
            return ContentTransferEncoding.BASE64;
        }
    }

    @Override
    void markDirty(Dirty dirty) {
        ContentTransferEncoding cte = ContentTransferEncoding.forString(getMimeHeader("Content-Transfer-Encoding"));
        ContentTransferEncoding cteCurrent = targetEncoding == null ? ContentTransferEncoding.BINARY : targetEncoding;
        if (cte.normalize() != cteCurrent.normalize()) {
            super.markDirty(dirty.combine(Dirty.CTE));
        } else {
            super.markDirty(dirty);
        }
        targetEncoding = cte;
    }
}
