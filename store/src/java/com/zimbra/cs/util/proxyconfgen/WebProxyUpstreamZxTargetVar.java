package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

class WebProxyUpstreamZxTargetVar extends ProxyConfVar {

  public WebProxyUpstreamZxTargetVar() {
    super(
        "web.upstream.schema",
        Provisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
        true,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.SERVER,
        "The target for zx paths");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    Boolean value = (Boolean) o;
    if (!value) {
      return "http://" + ProxyConfGen.ZIMBRA_UPSTREAM_ZX_NAME;
    } else {
      return "https://" + ProxyConfGen.ZIMBRA_SSL_UPSTREAM_ZX_NAME;
    }
  }
}
