package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;

class ZMSSOEnablerVar extends ProxyConfVar {

  public ZMSSOEnablerVar() {
    super(
        "web.sso.enabled",
        Provisioning.A_zimbraReverseProxyClientCertMode,
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
