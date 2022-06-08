package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;
import java.util.List;

class WebSslUpstreamZxServersVar extends ServersVar {

  public WebSslUpstreamZxServersVar() {
    super(
        "web.ssl.upstream.zx.:servers",
        "List of upstream HTTPS servers towards zx ssl port used by Web Proxy (i.e. servers "
            + "for which zimbraReverseProxyLookupTarget is true, and whose "
            + "mail mode is http|https|mixed|both)");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();

    List<Server> mailclientservers = mProv.getAllMailClientServers();
    for (Server server : mailclientservers) {
      String serverName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");

      if (isValidUpstream(server, serverName)) {
        directives.add(
            generateServerDirective(server, serverName, ProxyConfGen.ZIMBRA_UPSTREAM_SSL_ZX_PORT));
        mLog.debug("Added server to HTTPS zx ssl upstream: " + serverName);
      }
    }
    mValue = directives;
  }
}
