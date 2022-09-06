// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.cs.html.owasp.OwaspHtmlSanitizer;
import java.net.URI;
import java.net.URISyntaxException;
import org.owasp.html.AttributePolicy;

public class SrcAttributePolicy implements AttributePolicy {

  @Override
  public String apply(String elementName, String attributeName, String srcValue) {
    String base = OwaspHtmlSanitizer.zThreadLocal.get().getBaseHref();

    if (base != null && srcValue != null) {
      URI baseHrefURI = null;
      try {
        baseHrefURI = new URI(base);
      } catch (URISyntaxException e) {
        if (!base.endsWith("/")) base += "/";
      }
      if (srcValue.indexOf(":") == -1) {
        if (!srcValue.startsWith("/")) {
          srcValue = "/" + srcValue;
        }

        if (baseHrefURI != null) {
          try {
            srcValue = baseHrefURI.resolve(srcValue).toString();
            return srcValue;
          } catch (IllegalArgumentException e) {
            // ignore and do string-logic
          }
        }
        srcValue = base + srcValue;
      }
    }
    return srcValue;
  }
}
