package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;

class ZMSSODefaultEnablerVar extends ProxyConfVar {

  public ZMSSODefaultEnablerVar() {
    super(
        "web.sso.enabled",
        ZAttrProvisioning.A_zimbraReverseProxyClientCertMode,
        false,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "whether enable sso for global/server level");
  }

  @Override
  public void update() throws ServiceException {
    if (ProxyConfGen.isClientCertVerifyEnabled()) {
      mValue = true;
    } else {
      mValue = false;
    }
  }
}
