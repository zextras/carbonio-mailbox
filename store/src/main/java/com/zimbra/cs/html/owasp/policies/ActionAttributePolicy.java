// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.html.owasp.OwaspHtmlSanitizer;
import com.zimbra.cs.html.owasp.OwaspThreadLocal;
import java.net.MalformedURLException;
import java.net.URL;
import org.owasp.html.AttributePolicy;

public class ActionAttributePolicy implements AttributePolicy {

  // enable same host post request for a form in email
  private static boolean sameHostFormPostCheck = DebugConfig.defang_block_form_same_host_post_req;

  @Override
  public String apply(String elementName, String attributeName, String value) {
    // The Host header received in the request.
    OwaspThreadLocal threadLocalInstance = OwaspHtmlSanitizer.zThreadLocal.get();
    String reqVirtualHost = null;
    if (threadLocalInstance != null) {
      reqVirtualHost = OwaspHtmlSanitizer.zThreadLocal.get().getVHost();
    }
    if (sameHostFormPostCheck && reqVirtualHost != null) {
      try {
        URL url = new URL(value);
        String formActionHost = url.getHost().toLowerCase();

        if (formActionHost.equalsIgnoreCase(reqVirtualHost)) {
          value = value.replace(formActionHost, "SAMEHOSTFORMPOST-BLOCKED");
        }
      } catch (MalformedURLException e) {
        ZimbraLog.soap.warn("Error parsing URL, possible relative URL." + e.getMessage());
        value = "SAMEHOSTFORMPOST-BLOCKED";
      }
    }
    return value;
  }
}
