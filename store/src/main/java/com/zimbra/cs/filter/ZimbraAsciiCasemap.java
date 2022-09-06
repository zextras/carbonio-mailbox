// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import com.zimbra.common.soap.HeaderConstants;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.jsieve.comparators.AsciiCasemap;
import org.apache.jsieve.exception.FeatureException;
import org.apache.jsieve.exception.SievePatternException;

/**
 * Class ZimbraAsciiCasecomp enhances the jsieve's AsciiCasemap class to support the RFC 5231:
 * Relational Extension.
 */
public class ZimbraAsciiCasemap extends AsciiCasemap implements ZimbraComparator {

  public boolean values(String operator, String left, String right) throws FeatureException {
    switch (operator) {
      case HeaderConstants.GT_OP:
        return (left.compareToIgnoreCase(right) > 0);
      case HeaderConstants.GE_OP:
        return (left.compareToIgnoreCase(right) >= 0);
      case HeaderConstants.LT_OP:
        return (left.compareToIgnoreCase(right) < 0);
      case HeaderConstants.LE_OP:
        return (left.compareToIgnoreCase(right) <= 0);
      case HeaderConstants.EQ_OP:
        return (left.compareToIgnoreCase(right) == 0);
      case HeaderConstants.NE_OP:
        return (left.compareToIgnoreCase(right) != 0);
    }
    return false;
  }

  @Override
  public boolean counts(String operator, List<String> lhs, String rhs) throws FeatureException {
    throw new FeatureException("Substring counts unsupported by ascii-casemap");
  }

  /**
   * @see org.apache.jsieve.comparators.AsciiCasemap.matches#matches(String, String)
   */
  public boolean matches(String string, String glob) throws SievePatternException {
    try {
      String regex = FilterUtil.sieveToJavaRegex(glob.toUpperCase());
      final Matcher matcher =
          Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
              .matcher(string.toUpperCase());
      return matcher.matches();
    } catch (PatternSyntaxException e) {
      throw new SievePatternException(e.getMessage());
    }
  }

  /**
   * @see org.apache.jsieve.comparators.AsciiCasemap#equals(String, String)
   */
  @Override
  public boolean equals2(String string1, String string2) throws FeatureException {
    return equals(string1, string2);
  }
}
