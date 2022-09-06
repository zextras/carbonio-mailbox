package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;

/**
 * Provide the value of "proxy_pass" for web proxy.
 *
 * @author jiankuan
 */
class WebProxyUpstreamTargetVar extends ProxyConfVar {

  public WebProxyUpstreamTargetVar() {
    super(
        "web.upstream.schema",
        ZAttrProvisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
        true,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.SERVER,
        "The target of proxy_pass for web proxy");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    Boolean value = (Boolean) o;
    if (Boolean.FALSE.equals(value)) {
      return "http://" + ProxyConfGen.ZIMBRA_UPSTREAM_NAME;
    } else {
      return "https://" + ProxyConfGen.ZIMBRA_SSL_UPSTREAM_NAME;
    }
  }
}
