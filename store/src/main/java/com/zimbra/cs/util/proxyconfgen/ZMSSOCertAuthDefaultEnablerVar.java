package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;

class ZMSSOCertAuthDefaultEnablerVar extends ProxyConfVar {

  public ZMSSOCertAuthDefaultEnablerVar() {
    super(
        "web.sso.certauth.default.enabled",
        null,
        null,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "whether to turn on certauth in global/server level");
  }

  @Override
  public void update() throws ServiceException {
    String certMode =
        serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyClientCertMode, "off");
    if (certMode.equals("on") || certMode.equals("optional")) {
      mValue = true;
    } else {
      // ... we may add more condition if more sso auth method is introduced
      mValue = false;
    }
  }
}
