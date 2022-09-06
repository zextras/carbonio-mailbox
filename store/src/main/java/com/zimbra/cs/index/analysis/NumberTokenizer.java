// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * Numbers separated by ' ' or '\t'.
 *
 * @author tim
 * @author ysasaki
 */
public final class NumberTokenizer extends Tokenizer {

  private int endPos = 0;
  private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
  private OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);

  public NumberTokenizer(Reader reader) {
    super(reader);
  }

  @Override
  public boolean incrementToken() throws IOException {
    clearAttributes();

    int startPos = endPos;
    StringBuilder buf = new StringBuilder(10);

    while (true) {
      int c = input.read();
      endPos++;
      switch (c) {
        case -1:
          if (buf.length() == 0) {
            return false;
          }
          // no break!
        case ' ':
        case '\t':
          if (buf.length() != 0) {
            termAttr.setEmpty().append(buf);
            offsetAttr.setOffset(startPos, endPos - 1);
            return true;
          }
          break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          buf.append((char) c);
          break;
        default:
          // ignore char
      }
    }
  }
}
