// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Swallow dots, but include dots in a token only when it is not the only char
 * in the token.
 */
public final class ContactTokenFilter extends TokenFilter {
    private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

    public ContactTokenFilter(AddrCharTokenizer input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            if (termAttr.length() == 1 && termAttr.charAt(0) == '.') {
              // swallow dot
            } else {
                return true;
            }
        }
        return false;
    }

}
