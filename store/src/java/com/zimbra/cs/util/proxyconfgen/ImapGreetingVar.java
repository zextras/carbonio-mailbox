package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.BuildInfo;

class ImapGreetingVar extends ProxyConfVar {

  public ImapGreetingVar() {
    super(
        "mail.imap.greeting",
        Provisioning.A_zimbraReverseProxyImapExposeVersionOnBanner,
        "",
        ProxyConfValueType.STRING,
        ProxyConfOverride.CONFIG,
        "Proxy IMAP banner message (contains build version if "
            + Provisioning.A_zimbraReverseProxyImapExposeVersionOnBanner
            + " is true)");
  }

  @Override
  public void update() {
    if (serverSource.getBooleanAttr(
        Provisioning.A_zimbraReverseProxyImapExposeVersionOnBanner, false)) {
      mValue = "* OK " + "Zimbra " + BuildInfo.VERSION + " IMAP4 ready";
    } else {
      mValue = "";
    }
  }
}
