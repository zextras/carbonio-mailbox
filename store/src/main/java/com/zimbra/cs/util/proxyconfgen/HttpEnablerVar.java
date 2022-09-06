package com.zimbra.cs.util.proxyconfgen;

class HttpEnablerVar extends WebEnablerVar {

  public HttpEnablerVar() {
    super(
        "web.http.enabled",
        true,
        "Indicates whether HTTP Proxy will accept connections on HTTP "
            + "(true unless zimbraReverseProxyMailMode is 'https')");
  }

  @Override
  public void update() {
    String mailmode = getZimbraReverseProxyMailMode();
    if ("https".equalsIgnoreCase(mailmode)) {
      mValue = false;
    } else {
      mValue = true;
    }
  }
}
