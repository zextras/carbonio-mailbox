package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllMemcachedServersCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllMemcachedServersCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllMemcachedServers();
  }

  private void doGetAllMemcachedServers() throws ServiceException {
    var prov = provUtil.getProvisioning();
    var console = provUtil.getConsole();
    List<Server> servers = prov.getAllServers(Provisioning.SERVICE_MEMCACHED);
    for (Server server : servers) {
      console.print(
              server.getAttr(Provisioning.A_zimbraMemcachedBindAddress, "")
                      + ":"
                      + server.getAttr(Provisioning.A_zimbraMemcachedBindPort, "")
                      + " ");
    }
    console.println();
  }

}
