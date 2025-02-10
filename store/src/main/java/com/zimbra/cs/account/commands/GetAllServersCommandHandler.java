package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllServersCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllServersCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllServers(args);
  }

  private void doGetAllServers(String[] args) throws ServiceException {
    boolean verbose = false;
    boolean applyDefault = true;
    String service = null;

    var console = provUtil.getConsole();
    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else {
        if (service == null) {
          service = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified service: " + service);
          provUtil.usage();
          return;
        }
      }
      i++;
    }

    if (!applyDefault && !verbose) {
      console.println(ProvUtil.ERR_INVALID_ARG_EV);
      provUtil.usage();
      return;
    }

    List<Server> servers;
    if (provUtil.getProvisioning() instanceof SoapProvisioning soapProv) {
      servers = soapProv.getAllServers(service, applyDefault);
    } else {
      servers = provUtil.getProvisioning().getAllServers(service);
    }
    for (Server server : servers) {
      if (verbose) {
        dumper.dumpServer(server, applyDefault, null);
      } else {
        console.println(server.getName());
      }
    }
  }

}
