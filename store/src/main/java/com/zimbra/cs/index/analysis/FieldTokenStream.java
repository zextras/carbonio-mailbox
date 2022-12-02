// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.NumericUtils;

import com.google.common.base.Strings;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.LuceneFields;

/**
 * {@link TokenStream} for structured-data field.
 * <p>
 * {@code name:Val1 val2 val3} gets tokenized to {@code name:val1}, {@code name:val2}, {@code name:val3}. If the field
 * only consists of a single integer value, it produces an extra token of which name is appended by '#' to distinguish
 * from text search and the integer value gets encoded by Lucene's {@link NumericUtils}, so that it is also searchable
 * by numeric range query. Note that numeric fields are still tokenized as text too for wildcard search.
 *
 * @see LuceneFields#L_FIELD
 * @author tim
 * @author ysasaki
 */
public final class FieldTokenStream extends TokenStream {
    private static final int MAX_TOKEN_LEN = 100;
    private static final int MAX_TOKEN_COUNT = 1000;
    private static final Pattern NUMERIC_VALUE_REGEX = Pattern.compile("-?\\d+");

    private final List<String> tokens = new LinkedList<String>();
    private Iterator<String> iterator;
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

    public FieldTokenStream() {
    }

    public FieldTokenStream(String name, String value) {
        add(name, value);
    }

    public void add(String name, String value) {
        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(value)) {
            return;
        }

        name = normalizeName(name);

        if (NUMERIC_VALUE_REGEX.matcher(value).matches()) {
            try {
                add(name + "#:" + NumericUtils.intToPrefixCoded(Integer.parseInt(value)));
            } catch (NumberFormatException ignore) { // pass through
            }
        }

        if (value.equals("*")) { // wildcard alone
            add(name + ":*");
            return;
        }

        StringBuilder word = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // treat '-' as whitespace UNLESS it is at the beginning of a word
            if (isWhitespace(c) || (c == '-' && word.length() > 0)) {
                if (word.length() > 0) {
                    add(name + ':' + word.toString());
                    word.setLength(0);
                }
            } else if (!Character.isISOControl(c)) {
                word.append(Character.toLowerCase(c));
            }
        }

        if (word.length() > 0) {
            add(name + ':' + word.toString());
        }
    }

    private void add(String token) {
        if (token.length() <= MAX_TOKEN_LEN && tokens.size() < MAX_TOKEN_COUNT) {
            tokens.add(token);
        } else {
            ZimbraLog.index.debug("Unable to index: %.30s", token);
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        if (iterator == null) {
            iterator = tokens.iterator();
        }

        if (iterator.hasNext()) {
            termAttr.setEmpty().append(iterator.next());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset() {
        iterator = null;
    }

    @Override
    public void close() {
        tokens.clear();
    }

    private boolean isWhitespace(char ch) {
        switch (ch) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
        case '"': // conflict with query language
        case '\'':
        case ';':
        case ',':
        // case '-': don't remove - b/c of negative numbers!
        case '<':
        case '>':
        case '[':
        case ']':
        case '(':
        case ')':
        case '*': // wildcard conflict w/ query language
            return true;
        default:
            return false;
        }
    }

    /**
     * Strip control characters and ':', make it all lower-case.
     *
     * @param name raw field name
     * @return normalized field name
     */
    private String normalizeName(String name) {
        StringBuilder result = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isISOControl(c) && c != ':') {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

}
