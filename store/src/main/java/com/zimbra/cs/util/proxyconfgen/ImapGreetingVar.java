package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.util.BuildInfo;

class ImapGreetingVar extends ProxyConfVar {

  public ImapGreetingVar() {
    super(
        "mail.imap.greeting",
        ZAttrProvisioning.A_zimbraReverseProxyImapExposeVersionOnBanner,
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
        ZAttrProvisioning.A_zimbraReverseProxyImapExposeVersionOnBanner, false)) {
      mValue = "* OK " + "Zimbra " + BuildInfo.VERSION + " IMAP4 ready";
    } else {
      mValue = "";
    }
  }
}
