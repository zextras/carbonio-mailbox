package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.httpclient.URLUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllMtaAuthURLsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllMtaAuthURLsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllMtaAuthURLs();
  }

  private void doGetAllMtaAuthURLs() throws ServiceException {
    var prov = provUtil.getProvisioning();
    List<Server> servers = prov.getAllServers();
    var console = provUtil.getConsole();
    for (Server server : servers) {
      boolean isTarget = server.getBooleanAttr(ZAttrProvisioning.A_zimbraMtaAuthTarget, false);
      if (isTarget) {
        String url = URLUtil.getMtaAuthURL(server) + " ";
        console.print(url);
      }
    }
    console.println();
  }
}
