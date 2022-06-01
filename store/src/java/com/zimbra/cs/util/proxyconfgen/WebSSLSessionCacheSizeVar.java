package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

class WebSSLSessionCacheSizeVar extends ProxyConfVar {

  public WebSSLSessionCacheSizeVar() {
    super(
        "ssl.session.cachesize",
        Provisioning.A_zimbraReverseProxySSLSessionCacheSize,
        "10m",
        ProxyConfValueType.STRING,
        ProxyConfOverride.SERVER,
        "SSL session cache size for the proxy");
  }

  @Override
  public String format(Object o) {
    String sslSessionCacheSize = (String) o;
    return "shared:SSL:" + sslSessionCacheSize;
  }
}
