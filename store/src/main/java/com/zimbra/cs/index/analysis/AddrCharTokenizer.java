// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * Tokenizer for email addresses.
 *
 * <p>Normalization (accent folding, case folding, full-width → half-width) is applied by wrapping
 * this tokenizer with {@link NormalizeTokenFilter} as a {@code CharFilter} in the analyzer's
 * {@code createComponents} method.
 *
 * @author tim
 * @author ysasaki
 */
public final class AddrCharTokenizer extends CharTokenizer {

    public AddrCharTokenizer() {
        super();
    }

    @Override
    protected boolean isTokenChar(int ch) {
        if (Character.isWhitespace(ch)) {
            return false;
        }
        switch (ch) {
            case '\u3000': // fullwidth space
            case '<':
            case '>':
            case '\"':
            case ',':
            case '\'':
            case '(':
            case ')':
            case '[':
            case ']':
                return false;
        }
        return true;
    }

}
