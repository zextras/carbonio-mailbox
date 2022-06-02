package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import java.util.ArrayList;
import java.util.Collections;

/** Provide the value of "ssl_protocols" for web proxy. */
class WebSSLProtocolsVar extends ProxyConfVar {

  public WebSSLProtocolsVar() {
    super(
        "web.ssl.protocols",
        null,
        getEnabledSSLProtocols(),
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "SSL Protocols enabled for the web proxy");
  }

  static ArrayList<String> getEnabledSSLProtocols() {
    ArrayList<String> sslProtocols = new ArrayList<>();
    sslProtocols.add("TLSv1.2");
    sslProtocols.add("TLSv1.3");
    return sslProtocols;
  }

  @Override
  public void update() {

    ArrayList<String> sslProtocols = new ArrayList<>();
    String[] sslProtocolsEnabled =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxySSLProtocols);
    Collections.addAll(sslProtocols, sslProtocolsEnabled);
    if (!sslProtocols.isEmpty()) {
      mValue = sslProtocols;
    } else {
      mValue = mDefault;
    }
  }

  @Override
  public String format(Object o) {

    @SuppressWarnings("unchecked")
    ArrayList<String> sslProtocols = (ArrayList<String>) o;
    StringBuilder sslProto = new StringBuilder();
    for (String c : sslProtocols) {
      sslProto.append(" ");
      sslProto.append(c);
    }
    return sslProto.toString();
  }
}
