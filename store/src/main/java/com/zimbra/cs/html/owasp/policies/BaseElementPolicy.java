// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.cs.html.owasp.OwaspHtmlSanitizer;
import java.util.List;
import org.owasp.html.ElementPolicy;

public class BaseElementPolicy implements ElementPolicy {

  @Override
  public String apply(String elementName, List<String> attrs) {

    final int hrefIndex = attrs.indexOf("href");
    if (hrefIndex != -1) {
      String hrefValue = attrs.get(hrefIndex + 1);
      OwaspHtmlSanitizer.zThreadLocal.get().setBaseHref(hrefValue);
    }
    return null;
  }
}
