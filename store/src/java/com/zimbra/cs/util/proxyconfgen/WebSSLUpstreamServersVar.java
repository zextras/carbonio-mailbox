package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;
import java.util.List;

class WebSSLUpstreamServersVar extends ServersVar {

  public WebSSLUpstreamServersVar() {
    super(
        "web.ssl.upstream.:servers",
        "List of upstream HTTPS servers used by Web Proxy (i.e. servers "
            + "for which zimbraReverseProxyLookupTarget is true, and whose "
            + "mail mode is https|mixed|both)");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();
    String portName =
        configSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyHttpSSLPortAttribute, "");

    List<Server> mailclientservers = mProv.getAllMailClientServers();
    for (Server server : mailclientservers) {
      String serverName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");

      if (isValidUpstream(server, serverName)) {
        directives.add(generateServerDirective(server, serverName, portName));
        mLog.debug("Added server to HTTPS mailstore upstream: " + serverName);
      }
    }
    mValue = directives;
  }
}
