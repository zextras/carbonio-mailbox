// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import com.zimbra.cs.index.LuceneIndex;
import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;

/**
 * Tokenizer for email addresses.
 *
 * @author tim
 * @author ysasaki
 */
public final class AddrCharTokenizer extends CharTokenizer {

  public AddrCharTokenizer(Reader reader) {
    super(LuceneIndex.VERSION, reader);
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

  @Override
  protected int normalize(int c) {
    return (char) NormalizeTokenFilter.normalize(c);
  }
}
