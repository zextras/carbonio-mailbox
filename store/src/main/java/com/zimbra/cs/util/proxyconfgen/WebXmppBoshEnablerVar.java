package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;

class WebXmppBoshEnablerVar extends ProxyConfVar {

  public WebXmppBoshEnablerVar() {
    super(
        "web.xmpp.bosh.upstream.disable",
        ZAttrProvisioning.A_zimbraReverseProxyXmppBoshEnabled,
        false,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "whether to populate the location block for XMPP over BOSH requests to /http-bind path");
  }

  @Override
  public void update() throws ServiceException {
    String xmppEnabled =
        serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyXmppBoshEnabled, true);
    String xmppBoshLocalBindURL =
        serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyXmppBoshLocalHttpBindURL, true);
    String xmppBoshHostname =
        serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyXmppBoshHostname, true);
    int xmppBoshPort =
        serverSource.getIntAttr(ZAttrProvisioning.A_zimbraReverseProxyXmppBoshPort, 0);

    if (xmppBoshLocalBindURL == null
        || ProxyConfUtil.isEmptyString(xmppBoshLocalBindURL)
        || xmppBoshHostname == null
        || ProxyConfUtil.isEmptyString(xmppBoshHostname)
        || xmppBoshPort == 0) {
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
