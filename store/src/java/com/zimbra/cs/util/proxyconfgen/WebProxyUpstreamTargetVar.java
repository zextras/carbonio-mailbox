package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

/**
 * Provide the value of "proxy_pass" for web proxy.
 *
 * @author jiankuan
 */
class WebProxyUpstreamTargetVar extends ProxyConfVar {

  public WebProxyUpstreamTargetVar() {
    super(
        "web.upstream.schema",
        Provisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
        true,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.SERVER,
        "The target of proxy_pass for web proxy");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    Boolean value = (Boolean) o;
    if (!value) {
      return "http://" + ProxyConfGen.ZIMBRA_UPSTREAM_NAME;
    } else {
      return "https://" + ProxyConfGen.ZIMBRA_SSL_UPSTREAM_NAME;
    }
  }
}
