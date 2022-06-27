package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;

class WebProxyUpstreamZxTargetVar extends ProxyConfVar {

  public WebProxyUpstreamZxTargetVar() {
    super(
        "web.upstream.schema",
        ZAttrProvisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
        true,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.SERVER,
        "The target for zx paths");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    Boolean value = (Boolean) o;
    if (Boolean.FALSE.equals(value)) {
      return "http://" + ProxyConfGen.ZIMBRA_UPSTREAM_ZX_NAME;
    } else {
      return "https://" + ProxyConfGen.ZIMBRA_SSL_UPSTREAM_ZX_NAME;
    }
  }
}
