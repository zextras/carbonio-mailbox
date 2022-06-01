package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;

class WebXmppBoshEnablerVar extends ProxyConfVar {

  public WebXmppBoshEnablerVar() {
    super(
        "web.xmpp.bosh.upstream.disable",
        Provisioning.A_zimbraReverseProxyXmppBoshEnabled,
        false,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "whether to populate the location block for XMPP over BOSH requests to /http-bind path");
  }

  @Override
  public void update() throws ServiceException {
    String xmppEnabled =
        serverSource.getAttr(Provisioning.A_zimbraReverseProxyXmppBoshEnabled, true);
    String XmppBoshLocalBindURL =
        serverSource.getAttr(Provisioning.A_zimbraReverseProxyXmppBoshLocalHttpBindURL, true);
    String XmppBoshHostname =
        serverSource.getAttr(Provisioning.A_zimbraReverseProxyXmppBoshHostname, true);
    int XmppBoshPort = serverSource.getIntAttr(Provisioning.A_zimbraReverseProxyXmppBoshPort, 0);

    if (XmppBoshLocalBindURL == null
        || ProxyConfUtil.isEmptyString(XmppBoshLocalBindURL)
        || XmppBoshHostname == null
        || ProxyConfUtil.isEmptyString(XmppBoshHostname)
        || XmppBoshPort == 0) {
      mLog.debug(
          "web.xmpp.bosh.upstream.disable is false because one of the required attrs is unset");
      mValue = false;
    } else {
      if (xmppEnabled.equals("TRUE")) {
        mValue = true;
      } else {
        mValue = false;
      }
    }
  }
}
