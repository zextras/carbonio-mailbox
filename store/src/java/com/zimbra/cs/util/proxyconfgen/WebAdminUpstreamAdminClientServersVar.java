package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;
import java.util.List;

class WebAdminUpstreamAdminClientServersVar extends ProxyConfVar {

  public WebAdminUpstreamAdminClientServersVar() {
    super(
        "web.admin.upstream.:servers",
        null,
        null,
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "List of upstream HTTPS Admin client servers used by Web Proxy");
  }

  @Override
  public void update() throws ServiceException, ProxyConfException {
    ArrayList<String> directives = new ArrayList<>();
    String portName =
        configSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyAdminPortAttribute, "");

    List<Server> adminClientServers = mProv.getAllAdminClientServers();
    for (Server server : adminClientServers) {
      String serverName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");

      if (isValidUpstream(server, serverName)) {
        directives.add(generateServerDirective(server, serverName, portName));
        mLog.debug("Added server to HTTPS Admin client upstream: " + serverName);
      }
    }
    mValue = directives;
  }

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) o;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < servers.size(); i++) {
      String s = servers.get(i);
      if (i == 0) {
        sb.append(String.format("server    %s;%n", s));
      } else {
        sb.append(String.format("        server    %s;%n", s));
      }
    }
    return sb.toString();
  }
}
