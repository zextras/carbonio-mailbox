package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.fb.FbCli;
import org.apache.http.HttpException;

import java.io.IOException;

class PushFreebusyDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public PushFreebusyDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, HttpException, IOException {
    doPushFreeBusyForDomain(args);
  }

  private void doPushFreeBusyForDomain(String[] args)
          throws ServiceException, IOException, HttpException {
    provUtil.lookupDomain(args[1]);
    var prov = provUtil.getProvisioning();
    FbCli fbcli = new FbCli();
    for (Server server : prov.getAllMailClientServers()) {
      provUtil.getConsole().println("pushing to server " + server.getName());
      fbcli.setServer(server.getName());
      fbcli.pushFreeBusyForDomain(args[1]);
    }
  }
}
