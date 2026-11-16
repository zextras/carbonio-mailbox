// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import com.zimbra.common.mime.HeaderUtils.ByteBuilder;
import com.zimbra.common.util.CharsetUtil;

/**
 * Parses the value of a compound header (Content-Type, Content-Disposition) into its primary value
 * and its parameters, decoding RFC 2231 encoded parameters and assembling their continuations.
 * Shared by {@link MimeCompoundHeader} and {@code ZCompoundHeader}.
 */
public final class CompoundHeaderParser {
    private static final Charset DEFAULT_CHARSET = MimeHeader.DEFAULT_CHARSET;
    private static final String TSPECIALS = "()<>@,;:\\\"/[]?= \t";

    private enum State {
        PARAM, CONTINUED, EXTENDED, EQUALS, CHARSET, LANG, VALUE, QVALUE, SLOP, COMMENT
    }

    private record ParameterContinuation(String charset, boolean encoded, String value) {}

    private final Map<String, String> params;
    private final UnaryOperator<String> encodedWordDecoder;
    private final Map<String, Map<Integer, ParameterContinuation>> partials = HashMap.newHashMap(3);
    private State state = State.EQUALS;
    private State precomment;
    private ByteBuilder key = null;
    private ByteBuilder value = new ByteBuilder(DEFAULT_CHARSET);
    private ByteBuilder charset = null;
    private int comment = 0;
    private int continued = -1;
    private boolean encoded = false;
    private boolean quoted = false;
    private boolean escaped = false;

    private CompoundHeaderParser(Map<String, String> params, UnaryOperator<String> encodedWordDecoder) {
        this.params = params;
        this.encodedWordDecoder = encodedWordDecoder;
    }

    /**
     * Parses {@code content} from {@code start}, puts the parameters into {@code params} (names in
     * lower case) and returns the primary value.
     *
     * @param encodedWordDecoder decodes RFC 2047 encoded-words found in non-RFC 2231 parameter values
     */
    public static String parse(byte[] content, int start, Map<String, String> params,
            UnaryOperator<String> encodedWordDecoder) {
        CompoundHeaderParser parser = new CompoundHeaderParser(params, encodedWordDecoder);
        for (int i = start; i < content.length; i++) {
            parser.accept(content[i]);
        }
        parser.saveParameter();
        parser.assembleContinuations();
        return params.remove(null);
    }

    private void accept(byte b) {
        // ignore folding, even where it's not actually permitted
        if ((b == '\r' || b == '\n') && state != State.VALUE && state != State.SLOP) {
            escaped = false;
            return;
        }

        switch (state) {
            case QVALUE -> onQuotedValue(b);
            case PARAM -> onParam(b);
            case VALUE -> onValue(b);
            case EQUALS -> onEquals(b);
            case EXTENDED -> onExtended(b);
            case CONTINUED -> onContinued(b);
            case CHARSET -> onCharset(b);
            case LANG -> onLang(b);
            case COMMENT -> onComment(b);
            case SLOP -> onSlop(b);
        }
    }

    private void onQuotedValue(byte b) {
        if (!escaped && b == '\\') {
            escaped = true;
        } else if (!escaped && b == '"') {
            endParameter(State.SLOP);
        } else {
            value.append(b);
            escaped = false;
        }
    }

    private void onParam(byte b) {
        if (b == '=') {
            setState(State.EQUALS);
        } else if (b == '*') {
            setState(State.EXTENDED);
        } else if (b == '(') {
            startComment();
        } else if (b == ';') {
            endParameter(State.PARAM);
        } else if (b > 0x20 && b < 0x7F && TSPECIALS.indexOf(b) < 0) {
            key.append(b);
        }
    }

    private void onValue(byte b) {
        if (opensQuotedValue(b)) {
            quoted = true;
        } else if (closesQuotedValue(b)) {
            endParameter(State.SLOP);
        } else if (b == ';' || b == ' ' || b == '\t' || b == '\r' || b == '\n') {
            endParameter(b == ' ' || b == '\t' ? State.SLOP : State.PARAM);
        } else if (b == '(') {
            startComment();
        } else {
            value.append(b);
        }
    }

    private void onEquals(byte b) {
        if (b == ';') {
            endParameter(State.PARAM);
        } else if (b == '"') {
            escaped = false;
            setState(State.QVALUE);
        } else if (b == '(') {
            startComment();
        } else if (b != ' ' && b != '\t') {
            value.append(b);
            setState(State.VALUE);
        }
    }

    private void onExtended(byte b) {
        if (b >= '0' && b <= '9') {
            continued = b - '0';
            setState(State.CONTINUED);
        } else if (b == '=') {
            setEncoded();
            setState(continued > 0 ? State.VALUE : State.CHARSET);
        }
    }

    private void onContinued(byte b) {
        if (b == '=') {
            setState(State.EQUALS);
        } else if (b == '*') {
            setState(State.EXTENDED);
        } else if (b >= '0' && b <= '9') {
            continued = continued * 10 + b - '0';
        }
    }

    private void onCharset(byte b) {
        if (opensQuotedCharset(b)) {
            quoted = true;
        } else if (b == '\'') {
            setState(State.LANG);
        } else if (closesQuotedValue(b)) {
            endParameter(State.SLOP);
        } else if (b == ';') {
            endParameter(State.PARAM);
        } else {
            charset.append(b);
        }
    }

    private void onLang(byte b) {
        if (b == '\'') {
            setState(State.VALUE);
        } else if (closesQuotedValue(b)) {
            endParameter(State.SLOP);
        } else if (b == ';') {
            endParameter(State.PARAM);
        }
    }

    private void onComment(byte b) {
        if (escaped) {
            escaped = false;
        } else if (b == '\\') {
            escaped = true;
        } else if (b == '(') {
            comment++;
        } else if (b == ')' && --comment == 0) {
            setState(precomment);
        }
    }

    private void onSlop(byte b) {
        if (b == ';' || b == '\r' || b == '\n') {
            setState(State.PARAM);
        } else if (b == '(') {
            startComment();
        }
    }

    private void setState(State newstate) {
        if (newstate == State.COMMENT && state != State.COMMENT) {
            precomment = state;
        }
        state = newstate;
    }

    private void startComment() {
        escaped = false;
        comment++;
        setState(State.COMMENT);
    }

    private void endParameter(State next) {
        saveParameter();
        setState(next);
    }

    private void setEncoded() {
        encoded = true;
        if (continued <= 0) {
            charset = new ByteBuilder();
        }
    }

    // tolerate quotes around an encoded value (e.g. filename*0*="UTF-8''%D0%A2"), as other MUAs do
    private boolean opensQuotedCharset(byte b) {
        return b == '"' && !quoted && encoded && charset.isEmpty();
    }

    private boolean opensQuotedValue(byte b) {
        return b == '"' && !quoted && encoded && continued > 0 && value.isEmpty();
    }

    private boolean closesQuotedValue(byte b) {
        return b == '"' && quoted;
    }

    private void reset() {
        key = new ByteBuilder();
        value = new ByteBuilder(DEFAULT_CHARSET);
        continued = -1;
        encoded = false;
        quoted = false;
    }

    private void saveParameter() {
        try {
            String pname = key == null ? null : key.toString().toLowerCase();
            if (value == null || "".equals(pname)) {
                return;
            }
            if (continued >= 0) {
                savePartial(pname);
            } else {
                params.put(pname, decodeValue());
            }
        } finally {
            reset();
        }
    }

    // in order to handle out-of-order parts, store all partials until we're done
    private void savePartial(String pname) {
        Charset encoding = charset == null || charset.isEmpty()
                ? DEFAULT_CHARSET
                : MimeHeader.decodingCharset(CharsetUtil.toCharset(charset.toString()));
        partials.computeIfAbsent(pname, k -> new TreeMap<>())
                .put(continued, new ParameterContinuation(encoding.toString(), encoded, value.toString()));
        params.put(pname, null);
    }

    private String decodeValue() {
        String pvalue = value.toString();
        if (encoded) {
            return decodeRfc2231Value(pvalue);
        }
        if (pvalue.length() >= 8 && pvalue.contains("=?") && !pvalue.startsWith("?=") && pvalue.contains("?=")) {
            return encodedWordDecoder.apply(pvalue);
        }
        return pvalue;
    }

    private String decodeRfc2231Value(String pvalue) {
        String raw = pvalue;
        if (state == State.CHARSET) {
            // handle case where MUA left off charset/language in encoded param
            raw = value.append(charset).toString();
            charset.reset();
        }
        try {
            return new URLCodec(charset.isEmpty() ? "us-ascii" : charset.toString()).decode(raw);
        } catch (DecoderException e) {
            return raw;
        }
    }

    private void assembleContinuations() {
        for (Map.Entry<String, Map<Integer, ParameterContinuation>> entry : partials.entrySet()) {
            if (params.get(entry.getKey()) == null) {
                params.put(entry.getKey(), assemble(entry.getValue()));
            }
        }
    }

    private static String assemble(Map<Integer, ParameterContinuation> parts) {
        ParameterContinuation first = parts.get(0);
        String paramCharset = first == null ? "us-ascii" : first.charset();
        StringBuilder assembled = new StringBuilder();
        // consecutive encoded parts are decoded together: they don't necessarily break on a character boundary
        StringBuilder raw = new StringBuilder();
        for (ParameterContinuation partial : parts.values()) {
            if (partial.encoded()) {
                raw.append(partial.value());
            } else {
                appendDecoded(assembled, raw, paramCharset);
                assembled.append(partial.value());
            }
        }
        appendDecoded(assembled, raw, paramCharset);
        return assembled.toString();
    }

    private static void appendDecoded(StringBuilder assembled, StringBuilder raw, String paramCharset) {
        if (raw.isEmpty()) {
            return;
        }
        try {
            assembled.append(new URLCodec(paramCharset).decode(raw.toString()));
        } catch (DecoderException e) {
            assembled.append(raw);
        }
        raw.setLength(0);
    }
}
