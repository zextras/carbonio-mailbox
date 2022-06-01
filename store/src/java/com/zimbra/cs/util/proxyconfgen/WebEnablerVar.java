package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

abstract class WebEnablerVar extends ProxyConfVar {

  public WebEnablerVar(String keyword, Object defaultValue, String description) {
    super(
        keyword,
        null,
        defaultValue,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        description);
  }

  static String getZimbraReverseProxyMailMode() {
    return serverSource.getAttr(Provisioning.A_zimbraReverseProxyMailMode, "both");
  }
}
