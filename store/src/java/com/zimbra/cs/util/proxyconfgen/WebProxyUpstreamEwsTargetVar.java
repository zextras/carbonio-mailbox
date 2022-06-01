package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

class WebProxyUpstreamEwsTargetVar extends ProxyConfVar {

  public WebProxyUpstreamEwsTargetVar() {
    super(
        "web.upstream.schema",
        Provisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
        true,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.SERVER,
        "The ews target of proxy_pass for web proxy");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    Boolean value = (Boolean) o;
    if (!value) {
      return "http://" + ProxyConfGen.ZIMBRA_UPSTREAM_EWS_NAME;
    } else {
      return "https://" + ProxyConfGen.ZIMBRA_SSL_UPSTREAM_EWS_NAME;
    }
  }
}
