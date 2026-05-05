// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * Split by comma, space, CR, LF, dot.
 *
 * <p>Normalization (accent folding, case folding, full-width → half-width) is applied by wrapping
 * this tokenizer with {@link NormalizeTokenFilter} as a {@code CharFilter} in the analyzer's
 * {@code createComponents} method.
 *
 * @author tim
 * @author ysasaki
 */
public final class FilenameTokenizer extends CharTokenizer {

    public FilenameTokenizer() {
        super();
    }

    @Override
    protected boolean isTokenChar(int c) {
        switch (c) {
            case ',':
            case ' ':
            case '\r':
            case '\n':
            case '.':
                return false;
            default:
                return true;
        }
    }

}
