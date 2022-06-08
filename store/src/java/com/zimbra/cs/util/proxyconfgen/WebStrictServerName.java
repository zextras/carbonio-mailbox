package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;

class WebStrictServerName extends WebEnablerVar {

  public WebStrictServerName() {
    super(
        "web.strict.servername",
        "#",
        "Indicates whether the default server block is generated returning a default HTTP response"
            + " to all unknown hostnames");
  }

  @Override
  public String format(Object o) {
    if (isStrictEnforcementEnabled()) {
      return "";
    } else {
      return "#";
    }
  }

  public boolean isStrictEnforcementEnabled() {
    boolean enforcementEnabled =
        serverSource.getBooleanAttr(
            ZAttrProvisioning.A_zimbraReverseProxyStrictServerNameEnabled, false);
    mLog.info(String.format("Strict server name enforcement enabled? %s", enforcementEnabled));
    return enforcementEnabled;
  }
}
