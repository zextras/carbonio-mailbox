// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class QuotedStringParser {
  private String mInput;

  // the parser flips between these two sets of delimiters
  private static final String DELIM_WHITESPACE_AND_QUOTES = " \t\r\n\"";
  private static final String DELIM_QUOTES_ONLY = "\"";

  public QuotedStringParser(String input) {
    if (input == null) throw new IllegalArgumentException("Search Text cannot be null.");
    mInput = input;
  }

  public List<String> parse() {
    List<String> result = new ArrayList<String>();

    boolean returnTokens = true;
    String currentDelims = DELIM_WHITESPACE_AND_QUOTES;
    StringTokenizer parser = new StringTokenizer(mInput, currentDelims, returnTokens);

    boolean openDoubleQuote = false;
    boolean gotContent = false;
    String token = null;
    while (parser.hasMoreTokens()) {
      token = parser.nextToken(currentDelims);
      if (!isDoubleQuote(token)) {
        if (!currentDelims.contains(token)) {
          result.add(token);
          gotContent = true;
        }
      } else {
        currentDelims = flipDelimiters(currentDelims);
        // allow empty string in double quotes
        if (openDoubleQuote && !gotContent) result.add("");
        openDoubleQuote = !openDoubleQuote;
        gotContent = false;
      }
    }
    return result;
  }

  private boolean isDoubleQuote(String token) {
    return token.equals("\"");
  }

  private String flipDelimiters(String curDelims) {
    if (curDelims.equals(DELIM_WHITESPACE_AND_QUOTES)) return DELIM_QUOTES_ONLY;
    else return DELIM_WHITESPACE_AND_QUOTES;
  }
}
