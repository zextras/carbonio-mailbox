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
  private static List<String> servicesEnabled;
  private static final String webClientApp = "zimbra";
  private static final String webServiceApp = "service";
  private static final String adminClientApp = "zimbraAdmin";
  private static final String zimletApp = "zimlet";

  static {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      servicesEnabled = Arrays.asList(((String) envCtx.lookup("zimbraServicesEnabled")).split(","));
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

  public static boolean isZimbraServiceSplitEnabled() {
    if (!((servicesEnabled == null || servicesEnabled.isEmpty()) || allServicesEnabled())
        && servicesEnabled.contains(webServiceApp)) {
      ZimbraLog.misc.debug("service split enabled = true");
      return true;
    } else {
      ZimbraLog.misc.debug("service split enabled = false");
      return false;
    }
  }

  public static boolean isZimbraWebClientSplitEnabled() {
    if (!((servicesEnabled == null || servicesEnabled.isEmpty()) || allServicesEnabled())
        && servicesEnabled.contains(webClientApp)) {
      ZimbraLog.misc.debug("webclient split enabled = true");
      return true;
    } else {
      ZimbraLog.misc.debug("webclient split enabled = false");
      return false;
    }
  }

  private static boolean allServicesEnabled() {
    if (servicesEnabled.contains(webClientApp)
        && servicesEnabled.contains(webServiceApp)
        && servicesEnabled.contains(adminClientApp)
        && servicesEnabled.contains(zimletApp)) {
      ZimbraLog.misc.debug("all services enabled = true");
      return true;
    }
    ZimbraLog.misc.debug("all services enabled = false");
    return false;
  }
}
