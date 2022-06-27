package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import java.util.ArrayList;

class WebEwsUpstreamServersVar extends ServersVar {

  public WebEwsUpstreamServersVar() {
    super("web.upstream.ewsserver.:servers", "List of upstream EWS servers used by Web Proxy");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();
    String portName =
        configSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyHttpPortAttribute, "");
    String[] upstreams =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyUpstreamEwsServers);

    if (upstreams.length > 0) {
      for (String serverName : upstreams) {
        Server server = mProv.getServerByName(serverName);
        if (isValidUpstream(server, serverName)) {
          directives.add(generateServerDirective(server, serverName, portName));
          mLog.debug("Added EWS server to HTTP upstream: " + serverName);
        }
      }
    }
    mValue = directives;
  }
}
