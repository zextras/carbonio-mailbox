package com.zimbra.cs.util.proxyconfgen;

class HttpsEnablerVar extends WebEnablerVar {

  public HttpsEnablerVar() {
    super(
        "web.https.enabled",
        true,
        "Indicates whether HTTP Proxy will accept connections on HTTPS "
            + "(true unless zimbraReverseProxyMailMode is 'http')");
  }

  @Override
  public void update() {
    String mailMode = getZimbraReverseProxyMailMode();
    if ("http".equalsIgnoreCase(mailMode)) {
      mValue = false;
    } else {
      mValue = true;
    }
  }
}
