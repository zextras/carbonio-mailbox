package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import java.util.List;

class GetAllReverseProxyURLsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllReverseProxyURLsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllReverseProxyURLs();
  }

  private void doGetAllReverseProxyURLs() throws ServiceException {
    String REVERSE_PROXY_PROTO = ""; // don't need proto for nginx.conf
    String REVERSE_PROXY_PATH = ExtensionDispatcherServlet.EXTENSION_PATH + "/nginx-lookup";
    var prov = provUtil.getProvisioning();
    List<Server> servers = prov.getAllMailClientServers();
    var console = provUtil.getConsole();
    for (Server server : servers) {
      int port = server.getIntAttr(ZAttrProvisioning.A_zimbraExtensionBindPort, 7072);
      boolean isTarget =
              server.getBooleanAttr(ZAttrProvisioning.A_zimbraReverseProxyLookupTarget, false);
      if (isTarget) {
        String serviceName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");
        console.print(REVERSE_PROXY_PROTO + serviceName + ":" + port + REVERSE_PROXY_PATH + " ");
      }
    }
    console.println();
  }
}
