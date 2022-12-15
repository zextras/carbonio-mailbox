// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.HeaderUtils.ByteBuilder;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.util.ZimbraLog;

public class MimeHeader implements Cloneable {
    final HeaderInfo hinfo;
    protected final String name;
    protected byte[] content;
    protected int valueStart;

    /** Constructor for pre-analyzed header line read from message source.
     * @param name    Header field name.
     * @param content Complete raw header line, <b>including</b> the field name
     *                and colon and with folding and trailing CRLF and 2047-
     *                encoding intact.
     * @param start   The position within <code>content</code> where the header
     *                field value begins (after the ":"/": "). */
    MimeHeader(final String name, final byte[] content, final int start) {
        this.hinfo = HeaderInfo.of(name);
        this.name = name;
        this.content = content;
        this.valueStart = start;
    }

    /** Creates a {@code MimeHeader} from another {@code MimeHeader}. */
    MimeHeader(final MimeHeader header) {
        this.hinfo = header.hinfo;
        this.name = header.name;
        this.content = header.getRawHeader();
        this.valueStart = header.valueStart;
    }

    /** Creates a new {@code MimeHeader} with {@code value} as the field value.
     *  Header will be serialized as <tt>{name}: {encoded-value}CRLF</tt> after
     *  appropriate RFC 2047 encoded-word encoding and RFC 5322 line folding
     *  has been performed.  When generating encoded-words, <tt>utf-8</tt> will
     *  be used as the encoding charset.  <i>Note: No line folding is done at
     *  present.</i> */
    public MimeHeader(final String name, final String value) {
        this(name, value, null);
    }

    /** Creates a new {@code MimeHeader} with {@code value} as the field value.
     *  Header will be serialized as <tt>{name}: {encoded-value}CRLF</tt> after
     *  appropriate RFC 2047 encoded-word encoding and RFC 5322 line folding
     *  has been performed.  When generating encoded-words, {@code charset}
     *  will be used as the encoding charset if possible, defaulting back to
     *  <tt>utf-8</tt>.  <i>Note: No line folding is done at present.</i> */
    MimeHeader(final String name, final String value, final String charset) {
        this.hinfo = HeaderInfo.of(name);
        this.name = hinfo.name == null ? name : hinfo.name;
        updateContent(escape(value, CharsetUtil.toCharset(charset), charset, false).getBytes());
    }

    /** Creates a new {@code MimeHeader} serialized as "<tt>{name}:
     *  {bvalue}CRLF</tt>".  {@code bvalue} is copied verbatim; no charset
     *  transforms, encoded-word handling, or folding is performed. */
    public MimeHeader(final String name, final byte[] bvalue) {
        this.hinfo = HeaderInfo.of(name);
        this.name = hinfo.name == null ? name : hinfo.name;
        updateContent(bvalue);
    }

    @Override
    protected MimeHeader clone() {
        return new MimeHeader(this);
    }


    enum HeaderInfo {
        RETURN_PATH("Return-Path", 1, false, true),
        RECEIVED("Received", 2, false, true),
        RESENT_DATE("Resent-Date", 3, false, false, true),
        RESENT_FROM("Resent-From", 3, false, false, true),
        RESENT_SENDER("Resent-Sender", 3, false, false, true),
        RESENT_TO("Resent-To", 3, false, false, true),
        RESENT_CC("Resent-Cc", 3, false, false, true),
        RESENT_BCC("Resent-Bcc", 3, false, false, true),
        RESENT_MESSAGE_ID("Resent-Message-ID", 3, false, false, true),
        DATE("Date", 4, true),
        FROM("From", 5),
        SENDER("Sender", 6, true),
        REPLY_TO("Reply-To", 7, true),
        TO("To", 8),
        CC("Cc", 9),
        BCC("Bcc", 10),
        SUBJECT("Subject", 11, true),
        MESSAGE_ID("Message-ID", 12, true),
        IN_REPLY_TO("In-Reply-To", 13, true),
        REFERENCES("References", 14, true),
        THREAD_TOPIC("Thread-Topic", 15, true),
        THREAD_INDEX("Thread-Index", 16, true),
        CONTENT_TYPE("Content-Type", 17, true),
        CONTENT_DISPOSITION("Content-Disposition", 18, true),
        CONTENT_TRANSFER_ENCODING("Content-Transfer-Encoding", 19, true),
        MIME_VERSION("MIME-Version", 20, true),
        DEFAULT(null, 35),
        CONTENT_LENGTH("Content-Length", 49, true),
        STATUS("Status", 50, true);

        final String name;
        final int position;
        final boolean unique, prepend, first;

        HeaderInfo(String name, int position) {
            this(name, position, false, false, false);
        }

        HeaderInfo(String name, int position, boolean unique) {
            this(name, position, unique, false, false);
        }

        HeaderInfo(String name, int position, boolean unique, boolean prepend) {
            this(name, position, unique, prepend, false);
        }

        HeaderInfo(String name, int position, boolean unique, boolean prepend, boolean first) {
            this.name = name;  this.position = position;
            this.unique = unique;  this.prepend = prepend;  this.first = first;
        }

        private static final Map<String, HeaderInfo> lookup = new HashMap<String, HeaderInfo>(40);
        static {
            for (HeaderInfo hinfo : EnumSet.allOf(HeaderInfo.class)) {
                if (hinfo.name != null) {
                    lookup.put(hinfo.name.toLowerCase(), hinfo);
                }
            }
        }

        static HeaderInfo of(String name) {
            HeaderInfo hinfo = name == null ? null : lookup.get(name.toLowerCase());
            return hinfo == null ? DEFAULT : hinfo;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    /** Reserializes the {@code MimeHeader}, using {@code bvalue} as the
     *  field value (the bit after the '<tt>:</tt>').  {@code bvalue} is
     *  copied verbatim; no charset transforms, encoded-word handling, or
     *  folding is performed.*/
    MimeHeader updateContent(final byte[] bvalue) {
        byte[] bname = name.getBytes();
        int nlen = bname.length, vlen = bvalue == null ? 0 : bvalue.length;
        int csize = nlen + vlen + 4;

        byte[] buf = new byte[csize];
        System.arraycopy(bname, 0, buf, 0, nlen);
        buf[nlen] = ':';  buf[nlen + 1] = ' ';
        if (bvalue != null) {
            System.arraycopy(bvalue, 0, buf, nlen + 2, vlen);
        }
        buf[csize - 2] = '\r';  buf[csize - 1] = '\n';

        this.content = buf;  this.valueStart = nlen + 2;
        return this;
    }

    /** Returns this header's field name (the bit before the '<tt>:</tt>' in
     *  the header line). */
    public String getName() {
        return name;
    }

    /** Returns the entire header line (including the field name and the
     *  '<tt>:</tt>') as a raw byte array. */
    public byte[] getRawHeader() {
        reserialize();
        return content;
    }

    /** Returns the header's value (the bit after the '<tt>:</tt>') after all
     *  unfolding and decoding of RFC 2047 encoded-words has been performed. */
    public String getValue(final String charset) {
        reserialize();
        int end = content.length, c;
        while (end > valueStart && ((c = content[end-1]) == '\n' || c == '\r')) {
            end--;
        }
        return decode(content, valueStart, end - valueStart, CharsetUtil.toCharset(charset));
    }

    /** Returns the header's value (the bit after the '<tt>:</tt>') as a
     *  {@code String}.  No decoding is performed other than removing the
     *  trailing CRLF. */
    @Override
    public String toString() {
        return getEncodedValue(null);
    }

    /** Returns the header's value (the bit after the '<tt>:</tt>') as a
     *  {@code String}.  No decoding is performed other than removing the
     *  trailing CRLF. */
    public String getEncodedValue() {
        return getEncodedValue(null);
    }

    /** Returns the header's value (the bit after the '<tt>:</tt>') as a
     *  {@code String}.  If non-{@code null}, the {@code charset} is used when
     *  converting the header bytes to a {@code String}.  No decoding is
     *  performed other than removing the trailing CRLF. */
    public String getEncodedValue(String charset) {
        reserialize();
        int end = content.length, c;
        while (end > valueStart && ((c = content[end-1]) == '\n' || c == '\r')) {
            end--;
        }
        return createString(content, valueStart, end - valueStart, CharsetUtil.toCharset(charset));
    }

    private static String createString(byte[] bytes, int offset, int length, Charset charset) {
        return new String(bytes, offset, length, decodingCharset(charset));
    }

    /** Marks the header as "dirty" and requiring reserialization.  To enforce
     *  this reserialization requirement, unsets {@link #content}. */
    protected void markDirty() {
        this.content = null;
        this.valueStart = -1;
        // XXX: if header is in a header block, should mark that block as dirty?
    }

    /** Returns whether the header has been marked as needing reserialization.
     * @see #markDirty() */
    protected boolean isDirty() {
        return content == null;
    }

    /** Permits a subclass to regenerate the {@code byte[]} content of the
     *  header as a result of changes.  Implementations of this method should
     *  first call {@link #isDirty()} and perform a no-op if it returns
     *  {@code false}. */
    protected void reserialize() {
    }

    static final Charset DEFAULT_CHARSET = CharsetUtil.normalizeCharset(CharsetUtil.ISO_8859_1);

    static Charset decodingCharset(Charset charset) {
        return charset != null ? CharsetUtil.normalizeCharset(charset) : DEFAULT_CHARSET;
    }

    public static String decode(final String content) {
        return decode(content.getBytes(CharsetUtil.UTF_8), CharsetUtil.UTF_8);
    }

    static String decode(final byte[] content, final Charset charset) {
        return decode(content, 0, content.length, charset);
    }

    @SuppressWarnings("null")
    static String decode(final byte[] content, final int start, final int length, final Charset charset) {
        // short-circuit if there are only ASCII characters and no "=?"
        final int end = start + length;
        boolean complicated = false;
        for (int pos = start; pos < end && !complicated; pos++) {
            byte c = content[pos];
            if (c <= 0 || c >= 0x7F || (c == '=' && pos < end - 1 && content[pos + 1] == '?')) {
                complicated = true;
            }
        }
        if (!complicated) {
            return unfold(createString(content, start, length, charset));
        }

        ByteBuilder builder = new ByteBuilder(length, decodingCharset(charset));
        String value = null;
        boolean encoded = false;
        Boolean encwspenc = Boolean.FALSE;
        int questions = 0, wsplength = 0;

        for (int pos = start; pos < end; pos++) {
            byte c = content[pos];
            if (c == '\r' || c == '\n') {
                // ignore folding
            } else if (c == '=' && pos < end - 2 && content[pos + 1] == '?' && (!encoded || content[pos + 2] != '=')) {
                // "=?" marks the beginning of an encoded-word
                if (!builder.isEmpty()) {
                    value = builder.appendTo(value);
                }
                builder.reset();  builder.write('=');
                encoded = true;  questions = 0;
            } else if (c == '?' && encoded && ++questions > 3 && pos < end - 1 && content[pos + 1] == '=') {
                // "?=" may mean the end of an encoded-word, so see if it decodes correctly
                builder.write('?');  builder.write('=');
                String decoded = HeaderUtils.decodeWord(builder.toByteArray());
                boolean valid = decoded != null;
                if (valid) {
                    pos++;
                } else {
                    decoded = builder.pop().toString();
                }
                // drop all whitespace between encoded-words
                if (valid && encwspenc == Boolean.TRUE) {
                    value = value.substring(0, value.length() - wsplength);
                }
                value = value == null ? decoded : value + decoded;
                encwspenc = valid ? null : Boolean.FALSE;  wsplength = 0;
                encoded = false;  builder.reset();
            } else {
                builder.write(c);
                // track whitespace after encoded-words (enc wsp enc => remove wsp)
                boolean isWhitespace = c == ' ' || c == '\t';
                if (!encoded && encwspenc != Boolean.FALSE) {
                    encwspenc = isWhitespace;
                    wsplength = isWhitespace ? wsplength + 1 : 0;
                }
            }
        }

        if (!builder.isEmpty()) {
            value = builder.appendTo(value);
        }
        return value == null ? "" : value;
    }

    static String unfold(final String folded) {
        int length = folded.length(), i;
        for (i = 0; i < length; i++) {
            char c = folded.charAt(i);
            if (c == '\r' || c == '\n') {
                break;
            }
        }
        if (i == length) {
            return folded;
        }

        StringBuilder unfolded = new StringBuilder(length);
        if (i > 0) {
            unfolded.append(folded, 0, i);
        }
        while (++i < length) {
            char c = folded.charAt(i);
            if (c != '\r' && c != '\n') {
                unfolded.append(c);
            }
        }
        return unfolded.toString();
    }

    static class EncodedWord {

        static String encode(final String value, final String requestedCharsetName) {
            //bug 82851
            //used for special case where we want to label as one charset but encode as another
            //primarily for x-windows-iso2022jp vs iso-2022-jp internal/external mapping
            Charset charset = CharsetUtil.checkCharset(value, CharsetUtil.toCharset(requestedCharsetName));
            return encode(value, charset, requestedCharsetName);
        }

        private static String encode(final String value, Charset charset, String outputCharsetName) {
            StringBuilder sb = new StringBuilder();
            // FIXME: need to limit encoded-words to 75 bytes

            byte[] content = null;
            try {
                content = value.getBytes(charset);
            } catch (OutOfMemoryError e) {
                try {
                    ZimbraLog.system.fatal("out of memory", e);
                } finally {
                    Runtime.getRuntime().halt(1);
                    content = new byte[0];  // never reachable, but averts compiler warnings
                }
            } catch (Throwable t) {
                content = value.getBytes(CharsetUtil.ISO_8859_1);
                charset = CharsetUtil.ISO_8859_1;
                outputCharsetName = charset.name();
            }
            sb.append("=?").append(outputCharsetName == null ? charset.name().toLowerCase() : outputCharsetName.toLowerCase());

            int invalidQ = 0;
            for (byte b : content) {
                if (b < 0 || HeaderUtils.Q2047Encoder.FORCE_ENCODE[b]) {
                    invalidQ++;
                }
            }

            if (invalidQ > content.length / 3) {
                sb.append("?B?");
                sb.append(HeaderUtils.encodeB2047(content));
            } else {
                sb.append("?Q?");
                sb.append(HeaderUtils.encodeQ2047(content));
            }
            sb.append("?=");

            return sb.toString();
        }

        static String encode(final String value, final Charset requestedCharset) {
            Charset charset = CharsetUtil.checkCharset(value, requestedCharset);
            return encode(value, charset, charset.name());
        }
    }

    /** Characters that can form part of an RFC 5322 atom. */
    static final boolean[] ATEXT_VALID = new boolean[128];
    static {
        for (int c : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&'*+-/=?^_`{|}~".getBytes()) {
            ATEXT_VALID[c] = true;
        }
    }

    public static String escape(final String value, final Charset charset, final boolean phrase) {
        return escape(value, charset, null, phrase);
    }

    public static String escape(final String value, final Charset charset, final String requestedCharsetName, final boolean phrase) {
        boolean needsQuote = false, wsp = true;
        int needs2047 = 0, needsEscape = 0, cleanTo = 0, cleanFrom = value.length();
        for (int i = 0, len = value.length(); i < len; i++) {
            char c = value.charAt(i);
            if (c > 0x7F || c == '\0' || c == '\r' || c == '\n') {
                needs2047++;  cleanFrom = len;
            } else if (!phrase) {
                // if we're not in an RFC 5322 phrase, there is no such thing as "quoting"
            } else if (c == '"' || c == '\\') {
                needsQuote = true;  needsEscape++;  cleanFrom = len;
            } else if ((c != ' ' && !ATEXT_VALID[c]) || (c == ' ' && wsp)) {
                needsQuote = true;  cleanFrom = len;
            }
            wsp = c == ' ';
            if (wsp) {
                if (!needsQuote && needs2047 == 0 && i != len - 1) {
                    cleanTo = i + 1;
                } else if (cleanFrom == len && i > cleanTo + 1) {
                    cleanFrom = i;
                }
            }
        }
        if (phrase) {
            needsQuote |= wsp;
        }
        if (wsp) {
            cleanFrom = value.length();
        }

        if (needs2047 > 0) {
            String prefix = value.substring(0, cleanTo), suffix = value.substring(cleanFrom);
            String cleaned = value.substring(cleanTo, cleanFrom);
            String encoded = null;
            if (LC.mime_encode_compound_xwiniso2022jp_as_iso2022jp.booleanValue()
                            && requestedCharsetName != null
                            && charset != null
                            && requestedCharsetName.equalsIgnoreCase("ISO-2022-JP")
                            && charset.name().equalsIgnoreCase("x-windows-iso2022jp")) {
                encoded = EncodedWord.encode(cleaned, requestedCharsetName);
            } else {
                encoded = EncodedWord.encode(cleaned, charset);
            }
            return prefix + encoded + suffix;
        } else if (needsQuote && needsEscape > 0) {
            return quote(value, needsEscape);
        } else if (needsQuote) {
            return new StringBuilder(value.length() + 2).append('"').append(value).append('"').toString();
        } else {
            return value;
        }
    }

    static String quote(final String value) {
        return quote(value, 0);
    }

    private static String quote(final String value, final int escapeHint) {
        StringBuilder sb = new StringBuilder(value.length() + escapeHint + 2).append('"');
        for (int i = 0, len = value.length(); i < len; i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.append('"').toString();
    }
}
