package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;

class ClientCertAuthDefaultCAVar extends ProxyConfVar {

  public ClientCertAuthDefaultCAVar() {
    super(
        "ssl.clientcertca.default",
        Provisioning.A_zimbraReverseProxyClientCertCA,
        ProxyConfGen.getDefaultClientCertCaPath(),
        ProxyConfValueType.STRING,
        ProxyConfOverride.CUSTOM,
        "CA certificate for authenticating client certificates in nginx proxy (https only)");
  }

  @Override
  public void update() throws ServiceException {

    mValue = mDefault; // must be the value of getDefaultClientCertCaPath
  }
}
