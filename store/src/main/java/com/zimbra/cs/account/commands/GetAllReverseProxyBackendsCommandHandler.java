package com.zimbra.cs.account.commands;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

public class GetAllReverseProxyBackendsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllReverseProxyBackendsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllReverseProxyBackends();
  }

  private void doGetAllReverseProxyBackends() throws ServiceException {
    List<Server> servers = provUtil.getProvisioning().getAllServers();
    boolean atLeastOne = false;
    var console = provUtil.getConsole();
    for (Server server : servers) {
      boolean isTarget =
              server.getBooleanAttr(Provisioning.A_zimbraReverseProxyLookupTarget, false);
      if (!isTarget) {
        continue;
      }

      // (For now) assume HTTP can be load balanced to...
      String mode = server.getAttr(Provisioning.A_zimbraMailMode, null);
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
        backendPort = server.getIntAttr(Provisioning.A_zimbraMailPort, 0);
      } else {
        backendPort = server.getIntAttr(Provisioning.A_zimbraMailSSLPort, 0);
      }

      String serviceName = server.getAttr(Provisioning.A_zimbraServiceHostname, "");
      console.println("    server " + serviceName + ":" + backendPort + ";");
      atLeastOne = true;
    }

    if (!atLeastOne) {
      // workaround zmmtaconfig not being able to deal with empty output
      console.println("    server localhost:8080;");
    }
  }
}
