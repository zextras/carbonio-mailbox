// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.Arrays;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WebSplitUtil {

  private static final String WEB_SERVICE_APP = "service";
  private static List<String> servicesEnabled;

  static {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      String servicesEnabledStr = (String) envCtx.lookup("zimbraServicesEnabled");
      if (servicesEnabledStr != null && !servicesEnabledStr.isEmpty()) {
        servicesEnabled = Arrays.asList(servicesEnabledStr.split(","));
      } else {
        servicesEnabled = null;
      }

      if (servicesEnabled != null) {
        ZimbraLog.misc.debug("got services enabled %d", servicesEnabled.size());
        for (String service : servicesEnabled) {
          ZimbraLog.misc.debug("service=%s", service);
        }
      }
    } catch (NamingException e) {
      servicesEnabled = null;
      ZimbraLog.misc.debug("Naming exception while getting servicesEnabled", e);
    }
  }

  private WebSplitUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static boolean isZimbraServiceSplitEnabled() {
    if (servicesEnabled != null
        && !servicesEnabled.isEmpty()
        && servicesEnabled.contains(WEB_SERVICE_APP)) {
      ZimbraLog.misc.debug("service split enabled = true");
      return true;
    } else {
      ZimbraLog.misc.debug("service split enabled = false");
      return false;
    }
  }
}
