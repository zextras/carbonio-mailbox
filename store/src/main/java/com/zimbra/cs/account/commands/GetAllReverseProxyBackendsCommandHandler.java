package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.util.List;

class GetAllReverseProxyBackendsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllReverseProxyBackendsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllReverseProxyBackends();
  }

  private void doGetAllReverseProxyBackends() throws ServiceException {
    List<Server> servers = provUtil.getProvisioning().getAllServers();
    boolean atLeastOne = false;
    var console = provUtil.getConsole();
    for (Server server : servers) {
      boolean isTarget =
              server.getBooleanAttr(ZAttrProvisioning.A_zimbraReverseProxyLookupTarget, false);
      if (!isTarget) {
        continue;
      }

      // (For now) assume HTTP can be load balanced to...
      String mode = server.getAttr(ZAttrProvisioning.A_zimbraMailMode, null);
      if (mode == null) {
        continue;
      }
      Provisioning.MailMode mailMode = Provisioning.MailMode.fromString(mode);

      boolean isPlain =
              (mailMode == Provisioning.MailMode.http
                      || (!LC.zimbra_require_interprocess_security.booleanValue()
                      && (mailMode == Provisioning.MailMode.mixed
                      || mailMode == Provisioning.MailMode.both)));

      int backendPort;
      if (isPlain) {
        backendPort = server.getIntAttr(ZAttrProvisioning.A_zimbraMailPort, 0);
      } else {
        backendPort = server.getIntAttr(ZAttrProvisioning.A_zimbraMailSSLPort, 0);
      }

      String serviceName = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");
      console.println("    server " + serviceName + ":" + backendPort + ";");
      atLeastOne = true;
    }

    if (!atLeastOne) {
      // workaround zmmtaconfig not being able to deal with empty output
      console.println("    server localhost:8080;");
    }
  }
}
