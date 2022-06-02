package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.util.BuildInfo;

class Pop3GreetingVar extends ProxyConfVar {

  public Pop3GreetingVar() {
    super(
        "mail.pop3.greeting",
        ZAttrProvisioning.A_zimbraReverseProxyPop3ExposeVersionOnBanner,
        "",
        ProxyConfValueType.STRING,
        ProxyConfOverride.CONFIG,
        "Proxy IMAP banner message (contains build version if "
            + ZAttrProvisioning.A_zimbraReverseProxyImapExposeVersionOnBanner
            + " is true)");
  }

  @Override
  public void update() {
    if (serverSource.getBooleanAttr(
        ZAttrProvisioning.A_zimbraReverseProxyPop3ExposeVersionOnBanner, false)) {
      mValue = "+OK " + "Zimbra " + BuildInfo.VERSION + " POP3 ready";
    } else {
      mValue = "";
    }
  }
}
