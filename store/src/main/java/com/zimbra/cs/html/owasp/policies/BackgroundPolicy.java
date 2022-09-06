// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import com.zimbra.common.util.ZimbraLog;
import java.util.List;
import org.owasp.html.ElementPolicy;

public class BackgroundPolicy implements ElementPolicy {

  @Override
  public String apply(String elementName, List<String> attrs) {
    final int bgIndex = attrs.indexOf("background");
    if (bgIndex == -1) {
      return elementName;
    }
    if (bgIndex % 2 != 0) {
      ZimbraLog.mailbox.debug(
          "Keyword 'background' found as attribute value instead of attribute name, so ignoring.");
      return elementName;
    }

    String bgValue = attrs.get(bgIndex + 1);
    attrs.remove(bgIndex);
    attrs.remove(bgIndex); // value
    attrs.add("dfbackground");
    attrs.add(bgValue);
    return elementName;
  }
}
