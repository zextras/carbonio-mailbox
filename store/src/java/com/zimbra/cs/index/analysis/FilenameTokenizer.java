// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

import com.zimbra.cs.index.LuceneIndex;

/**
 * Split by comma, space, CR, LF, dot.
 *
 * @author tim
 * @author ysasaki
 */
public final class FilenameTokenizer extends CharTokenizer {

    public FilenameTokenizer(Reader reader) {
        super(LuceneIndex.VERSION, reader);
    }

    @Override
    protected boolean isTokenChar(char c) {
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

    @Override
    protected char normalize(char c) {
        return (char) NormalizeTokenFilter.normalize(c);
    }

}
