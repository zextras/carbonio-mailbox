package com.zimbra.cs.util.proxyconfgen;

import java.io.File;

class WebSSLDhparamEnablerVar extends WebEnablerVar {

  private final ProxyConfVar webSslDhParamFile;

  public WebSSLDhparamEnablerVar(ProxyConfVar webSslDhParamFile) {
    super(
        "web.ssl.dhparam.enabled",
        false,
        "Indicates whether ssl_dhparam directive should be added or not");
    this.webSslDhParamFile = webSslDhParamFile;
  }

  @Override
  public void update() {
    String dhparam = (String) webSslDhParamFile.rawValue();
    if (dhparam == null || ProxyConfUtil.isEmptyString(dhparam) || !(new File(dhparam).exists())) {
      mValue = false;
    } else {
      mValue = true;
    }
  }
}
