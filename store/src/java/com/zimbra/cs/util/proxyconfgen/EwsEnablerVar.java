package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.cs.account.Provisioning;

/**
 * @author zimbra
 */
class EwsEnablerVar extends WebEnablerVar {

  public EwsEnablerVar() {
    super(
        "web.ews.upstream.disable",
        "#",
        "Indicates whether EWS upstream servers blob in nginx.conf.web should be populated "
            + "(false unless zimbraReverseProxyUpstreamEwsServers is populated)");
  }

  @Override
  public String format(Object o) {
    String[] upstreams =
        serverSource.getMultiAttr(Provisioning.A_zimbraReverseProxyUpstreamEwsServers);
    if (upstreams.length == 0) {
      return "#";
    } else {
      return "";
    }
  }
}
