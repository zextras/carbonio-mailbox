// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.apache.logging.log4j.core.layout.PatternLayout.DEFAULT_CONVERSION_PATTERN;

import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PatternParser;

/**
 * Subclasses Log4J's <tt>PatternLayout</tt> class to add additional support for the <tt>%z</tt>
 * option, which prints the value returned by {@link ZimbraLog#getContextString()}.
 *
 * @author bburtin
 */
public class ZimbraPatternLayout {

  /*
  If you're wondering why this is here, there's no reason really other than I ported this codebase from Log4j1 to Log4j2 and wanted to maintain a sort of compatibility/same flow
   */
  private final PatternLayout patternLayout;

  public ZimbraPatternLayout() {
    this.patternLayout = PatternLayout.createDefaultLayout();
  }

  public ZimbraPatternLayout(String pattern) {
    this.patternLayout = PatternLayout.newBuilder().withPattern(pattern).build();
  }

  public PatternParser createPatternParser(String pattern) {
    if (pattern == null) {
      pattern = DEFAULT_CONVERSION_PATTERN;
    }

    return new PatternParser(pattern);
  }
}
