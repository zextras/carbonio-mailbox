// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.zimbra.cs.index.LuceneFields;

/**
 * {@code image/jpeg} becomes {@code image/jpeg} and {@code image}
 *
 * @author ysasaki
 */
public final class MimeTypeTokenStream extends TokenStream {
    private static final int MIN_TOKEN_LEN = 3;
    private static final int MAX_TOKEN_LEN = 256;
    private static final int MAX_TOKEN_COUNT = 100;

    private final List<String> tokens = new LinkedList<>();
    private Iterator<String> itr;
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

    public MimeTypeTokenStream() {
        tokens.add(LuceneFields.L_ATTACHMENT_NONE);
    }

    public MimeTypeTokenStream(String src) {
        add(src);
        tokens.add(tokens.isEmpty() ? LuceneFields.L_ATTACHMENT_NONE : LuceneFields.L_ATTACHMENT_ANY);
    }

    public MimeTypeTokenStream(Collection<String> list) {
        for (String src : list) {
            add(src);
        }
        tokens.add(tokens.isEmpty() ? LuceneFields.L_ATTACHMENT_NONE : LuceneFields.L_ATTACHMENT_ANY);
    }

    private void add(String src) {
        if (tokens.size() >= MAX_TOKEN_COUNT) {
            return;
        }
        String token = src.trim();
        if (token.length() < MIN_TOKEN_LEN || token.length() > MAX_TOKEN_LEN) {
            return;
        }
        token = token.toLowerCase();
        tokens.add(token);
        // extract primary of primary/sub
        int delim = token.indexOf('/');
        if (delim > 0) {
            String primary = token.substring(0, delim).trim();
            if (primary.length() >= MIN_TOKEN_LEN) {
                tokens.add(primary);
            }
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (itr.hasNext()) {
            termAttr.setEmpty().append(itr.next());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset() {
        itr = tokens.iterator();
    }

    @Override
    public void close() {
        tokens.clear();
    }

}
