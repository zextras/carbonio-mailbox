// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import com.zimbra.common.localconfig.DebugConfig;
import java.util.regex.Pattern;
import org.apache.xerces.xni.XMLString;
import org.cyberneko.html.filters.Purifier;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * @author zimbra
 */
public class HtmlPurifier extends Purifier {

  private static final Pattern VALID_IMG_TAG =
      Pattern.compile(DebugConfig.defangOwaspValidImgTag, Pattern.CASE_INSENSITIVE);
  private static final PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.IMAGES);
  private static final Pattern IMG_SKIP_OWASPSANITIZE =
      Pattern.compile(DebugConfig.defangImgSkipOwaspSanitize, Pattern.CASE_INSENSITIVE);
  private static final Pattern VALID_ONLOAD_METHOD =
      Pattern.compile(DebugConfig.defangOnloadMethod, Pattern.CASE_INSENSITIVE);

  /* (non-Javadoc)
   * @see org.cyberneko.html.filters.Purifier#purifyText(org.apache.xerces.xni.XMLString)
   */
  @Override
  protected XMLString purifyText(XMLString text) {
    String temp = text.toString();

    if (IMG_SKIP_OWASPSANITIZE.matcher(temp).find()) {
      return text;
    }

    if (VALID_IMG_TAG.matcher(temp).find()) {
      temp = sanitizer.sanitize(temp);
    }

    if (VALID_ONLOAD_METHOD.matcher(temp).find()) {
      temp = sanitizer.sanitize(temp);
    }

    XMLString n = new XMLString();
    n.setValues(temp.toCharArray(), 0, temp.length());

    return super.purifyText(n);
  }
}
