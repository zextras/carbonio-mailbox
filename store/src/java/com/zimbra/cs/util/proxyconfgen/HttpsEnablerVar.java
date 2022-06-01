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
    String mailmode = getZimbraReverseProxyMailMode();
    if ("http".equalsIgnoreCase(mailmode)) {
      mValue = false;
    } else {
      mValue = true;
    }
  }
}
