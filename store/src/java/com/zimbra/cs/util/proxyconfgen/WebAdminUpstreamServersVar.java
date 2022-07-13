package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

class WebAdminUpstreamServersVar extends ServersVar {

  public WebAdminUpstreamServersVar() {
    super(
        "web.admin.upstream.:servers",
        "List of upstream admin console servers used by Web Proxy (i.e. servers "
            + "for which zimbraReverseProxyLookupTarget is true");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();
    String portName =
        configSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyAdminPortAttribute, "");

    List<Server> uniqueMailClientServers =
        mProv.getAllMailClientServers().stream()
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toCollection(
                        () ->
                            new TreeSet<>(
                                Comparator.comparing(
                                    server -> server.getAttr(ZAttrProvisioning.A_zimbraId)))),
                    ArrayList::new));

    for (Server server : uniqueMailClientServers) {
      String serverName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");

      if (isValidUpstream(server, serverName)) {
        directives.add(generateServerDirective(server, serverName, portName));
        mLog.debug("Added server to HTTPS Admin mailstore upstream: " + serverName);
      }
    }
    mValue = directives;
  }
}
