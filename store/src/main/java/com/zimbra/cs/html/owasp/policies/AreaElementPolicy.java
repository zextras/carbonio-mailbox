// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.cs.html.owasp.OwaspHtmlSanitizer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.owasp.html.ElementPolicy;

public class AreaElementPolicy implements ElementPolicy {

  /** make sure all <area> tags have a target="_blank" attribute set. */
  @Override
  public String apply(String elementName, List<String> attrs) {
    int hrefIndex = attrs.indexOf("href");
    if (hrefIndex == -1) {
      // links that don't have a href don't need target="_blank"
      return "area";
    }
    String hrefValue = attrs.get(hrefIndex + 1);
    // LOCAL links don't need target="_blank"
    if (hrefValue.indexOf('#') == 0) {
      return "area";
    }
    final int targetIndex = attrs.indexOf("target");
    if (targetIndex != -1) {
      attrs.remove(targetIndex);
      attrs.remove(targetIndex); // value
    }
    attrs.add("target");
    attrs.add("_blank");

    hrefIndex = attrs.indexOf("href");
    hrefValue = attrs.get(hrefIndex + 1);
    String base = OwaspHtmlSanitizer.zThreadLocal.get().getBaseHref();
    if (base != null && hrefValue != null) {
      URI baseHrefURI = null;
      try {
        baseHrefURI = new URI(base);
      } catch (URISyntaxException e) {
        if (!base.endsWith("/")) base += "/";
      }
      if (hrefValue.indexOf(":") == -1) {
        if (!hrefValue.startsWith("/")) {
          hrefValue = "/" + hrefValue;
        }

        if (baseHrefURI != null) {
          try {
            hrefValue = baseHrefURI.resolve(hrefValue).toString();
            attrs.remove(hrefIndex);
            attrs.remove(hrefIndex); // value
            attrs.add("href");
            attrs.add(hrefValue);
            return "area";
          } catch (IllegalArgumentException e) {
            // ignore and do string-logic
          }
        }
        hrefValue = base + hrefValue;
        attrs.remove(hrefIndex);
        attrs.remove(hrefIndex); // value
        attrs.add("href");
        attrs.add(hrefValue);
      }
    }
    return "area";
  }
}
