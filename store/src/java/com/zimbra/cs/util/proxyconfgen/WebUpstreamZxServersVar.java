package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;
import java.util.List;

class WebUpstreamZxServersVar extends ServersVar {

  public WebUpstreamZxServersVar() {
    super(
        "web.upstream.zx.:servers",
        "List of upstream HTTP servers towards zx port used by Web Proxy (i.e. servers "
            + "for which zimbraReverseProxyLookupTarget is true, and whose "
            + "mail mode is http|https|mixed|both)");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();

    List<Server> mailClientServers = mProv.getAllMailClientServers();
    for (Server server : mailClientServers) {
      String serverName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");

      if (isValidUpstream(server, serverName)) {
        directives.add(
            generateServerDirective(server, serverName, ProxyConfGen.ZIMBRA_UPSTREAM_ZX_PORT));
        mLog.debug("Added server to HTTP zx upstream: " + serverName);
      }
    }
    mValue = directives;
  }
}
