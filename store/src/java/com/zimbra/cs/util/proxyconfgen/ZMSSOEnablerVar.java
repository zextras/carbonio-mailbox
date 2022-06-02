package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;

class ZMSSOEnablerVar extends ProxyConfVar {

  public ZMSSOEnablerVar() {
    super(
        "web.sso.enabled",
        ZAttrProvisioning.A_zimbraReverseProxyClientCertMode,
        false,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "whether enable sso for domain level");
  }

  @Override
  public void update() throws ServiceException {
    if (ProxyConfGen.isDomainClientCertVerifyEnabled()) {
      mValue = true;
    } else {
      mValue = false;
    }
  }
}
