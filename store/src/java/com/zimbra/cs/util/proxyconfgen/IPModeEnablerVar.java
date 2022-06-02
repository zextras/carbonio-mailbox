package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;

abstract class IPModeEnablerVar extends ProxyConfVar {

  private static IPMode currentIPMode = IPMode.UNKNOWN;

  protected IPModeEnablerVar(String keyword, Object defaultValue, String description) {
    super(
        keyword,
        null,
        defaultValue,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        description);
  }

  static IPMode getZimbraIPMode() {
    if (currentIPMode == IPMode.UNKNOWN) {
      String res = ProxyConfVar.serverSource.getAttr(ZAttrProvisioning.A_zimbraIPMode, "both");
      if (res.equalsIgnoreCase("both")) {
        currentIPMode = IPMode.BOTH;
      } else if (res.equalsIgnoreCase("ipv4")) {
        currentIPMode = IPMode.IPV4_ONLY;
      } else {
        currentIPMode = IPMode.IPV6_ONLY;
      }
    }

    return currentIPMode;
  }

  enum IPMode {
    UNKNOWN,
    BOTH,
    IPV4_ONLY,
    IPV6_ONLY
  }
}
