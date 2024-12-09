package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class ResetAllLoggersCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ResetAllLoggersCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doResetAllLoggers(args);
  }

  private void doResetAllLoggers(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sprov = (SoapProvisioning) prov;
    String server = null;
    if (args.length > 1 && ("-s".equals(args[1]) || "--server".equals(args[1]))) {
      server = args.length > 0 ? args[2] : null;
    }
    sprov.resetAllLoggers(server);
  }

}

