// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.net.URLCodec;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.HeaderUtils.ByteBuilder;
import com.zimbra.common.util.CharsetUtil;

public class MimeCompoundHeader extends MimeHeader {
    private boolean use2231Encoding;
    private String primaryValue;
    private Map<String, String> params = new LinkedHashMap<>();
    private String charset;

    protected MimeCompoundHeader(String name, String value) {
        this(name, value, false);
    }

    protected MimeCompoundHeader(String name, String value, boolean use2231) {
        super(name, value == null ? null : getBytes(value));
        this.use2231Encoding = use2231;
        parse();
    }

    protected MimeCompoundHeader(MimeHeader header) {
        super(header);
        if (header instanceof MimeCompoundHeader) {
            MimeCompoundHeader mch = (MimeCompoundHeader) header;
            this.use2231Encoding = mch.use2231Encoding;
            this.primaryValue    = mch.primaryValue;
            this.charset         = mch.charset;
            this.params.putAll(mch.params);
        } else {
            parse();
        }
    }

    protected MimeCompoundHeader(String name, byte[] content, int start) {
        super(name, content, start);
        parse();
    }

    private static byte[] getBytes(String value) {
        return value == null ? null : value.getBytes(DEFAULT_CHARSET);
    }


    private void parse() {
        if (content != null) {
            this.primaryValue = CompoundHeaderParser.parse(content, valueStart, params, MimeHeader::decode);
        }
    }

    public String getPrimaryValue() {
        return primaryValue;
    }

    public MimeCompoundHeader setPrimaryValue(String value) {
        if (value != null && !value.equals(primaryValue)) {
            markDirty();
            this.primaryValue = value;
        }
        return this;
    }

    public int getParameterCount() {
        return params.size();
    }

    public boolean containsParameter(String pname) {
        return getParameter(pname) != null;
    }

    public String getParameter(String pname) {
        if (pname != null) {
            // RFC 2045 2: "All media type values, subtype values, and parameter names
            //              as defined are case-insensitive."
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (pname.equalsIgnoreCase(param.getKey()))
                    return param.getValue();
            }
        }
        return null;
    }

    public MimeCompoundHeader setParameter(String name, String value) {
        markDirty();

        // massage the parameter name until it's valid
        String key = name;
        if (name != null) {
            StringBuilder sb = new StringBuilder(name.length());
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c > 0x20 && c < 0xFF && !TSPECIALS[c]) {
                    sb.append(c);
                }
            }
            key = sb.toString();
        }

        if (key != null && !key.isEmpty()) {
            // RFC 2045 2: "All media type values, subtype values, and parameter names
            //              as defined are case-insensitive."
            for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
                if (key.equalsIgnoreCase(it.next())) {
                    it.remove();
                }
            }
            if (value != null) {
                params.put(key, value);
            }
        }
        return this;
    }

    public Iterator<Map.Entry<String, String>> parameterIterator() {
        return params.entrySet().iterator();
    }

    public MimeCompoundHeader setCharset(String charset) {
        this.charset = charset == null || charset.trim().isEmpty() ? null : charset;
        return this;
    }

    public MimeCompoundHeader setUse2231Encoding(boolean use2231) {
        this.use2231Encoding = use2231;
        return this;
    }

    public MimeCompoundHeader cleanup() {
        markDirty();
        return this;
    }

    /** Printable ASCII characters that must be quoted in parameter values
     *  and avoided in parameter names. */
    private static final boolean[] TSPECIALS = new boolean[128];
    static {
        TSPECIALS['('] = TSPECIALS[')'] = TSPECIALS[','] = true;
        TSPECIALS['<'] = TSPECIALS['>'] = TSPECIALS['?'] = true;
        TSPECIALS['['] = TSPECIALS[']'] = TSPECIALS['='] = true;
        TSPECIALS[':'] = TSPECIALS[';'] = TSPECIALS['"'] = true;
        TSPECIALS['/'] = TSPECIALS['\\'] = TSPECIALS['@'] = true;
        TSPECIALS[' '] = TSPECIALS['\t'] = true;
    }

    private static final int LINE_WRAP_LENGTH = 76;

    @Override
    protected void reserialize() {
        if (content != null) {
            return;
        }

        ByteBuilder line = new ByteBuilder();
        line.append(name.getBytes()).append(':').append(' ');
        this.valueStart = line.length();

        if (primaryValue != null) {
            line.append(primaryValue);
        }

        int position = line.length();
        for (Map.Entry<String, String> param : params.entrySet()) {
            String key = param.getKey(), value = param.getValue();
            if (value == null || key == null || key.isEmpty()) {
                continue;
            }
            line.append(';');
            position++;

            boolean quoted = false, nonascii = false;
            for (int i = 0, max = value.length(); i < max; i++) {
                char c = value.charAt(i);
                if (c >= 0x7F || c < 0x20 && c != '\t') {
                    nonascii = true;
                    break;
                } else if (TSPECIALS[c]) {
                    quoted = true;
                }
            }

            String paramCharset = nonascii ? CharsetUtil.checkCharset(value, this.charset) : "us-ascii";

            ByteBuilder bb = new ByteBuilder();
            if (!nonascii) {
                if (quoted || value.isEmpty()) {
                    bb.append(param.getKey()).append('=').append('"');
                    for (int i = 0, max = value.length(); i < max; i++) {
                        char c = value.charAt(i);
                        if (c == '"' || c == '\\') {
                            bb.append('\\');
                        }
                        bb.append(c);
                    }
                    bb.append('"');
                } else {
                    bb.append(param.getKey()).append('=').append(value);
                }
            } else if (use2231Encoding) {
                try {
                    String encoded = new URLEncoder().encode(value, paramCharset);
                    bb.append(param.getKey()).append('*').append('=').append(paramCharset).append('\'').append('\'').append(encoded);
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                String encoded = null;
                Charset mappedCharset = CharsetUtil.toCharset(paramCharset);
                if (LC.mime_encode_compound_xwiniso2022jp_as_iso2022jp.booleanValue()
                        && paramCharset.equalsIgnoreCase("ISO-2022-JP")
                        && mappedCharset.name().equalsIgnoreCase("x-windows-iso2022jp")) {
                    //bug 82851
                    //usually paramCharset and CharsetUtil.toCharset(paramCharset).name() are equivalent
                    //however, if -Dsun.nio.cs.map is used then CharsetUtil.toCharset() returns a different charset
                    //we need to label the MIME with universally understood charset; even though we're mapping that
                    //internally to a variant that Java understands
                    encoded = EncodedWord.encode(value, paramCharset);
                } else {
                    encoded = EncodedWord.encode(value, mappedCharset);
                }
                bb.append(param.getKey()).append('=').append('"').append(encoded).append('"');
            }

            if (position + bb.length() > LINE_WRAP_LENGTH) {
                line.append('\r').append('\n').append(' ');
                position = 1;
            } else {
                line.append(' ');
                position++;
            }
            line.append(bb);
            position += bb.length();
        }

        this.content = line.append('\r').append('\n').toByteArray();
    }

    // special subclass of URLCodec that replaces ' ' with "%20" rather than with "+"
    private static class URLEncoder extends URLCodec {
        URLEncoder() {}

        private static final BitSet WWW_URL = (BitSet) WWW_FORM_URL.clone();
        static {
            WWW_URL.clear(' ');
        }

        @Override public byte[] encode(byte[] bytes) {
            return encodeUrl(WWW_URL, bytes);
        }
    }
}
