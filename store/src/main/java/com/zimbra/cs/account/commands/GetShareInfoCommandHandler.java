package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class GetShareInfoCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetShareInfoCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetShareInfo(args);
  }

  private void doGetShareInfo(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    Account owner = provUtil.lookupAccount(args[1]);

    provUtil.getConsole().println(ShareInfoVisitor.getPrintHeadings());
    prov.getShareInfo(owner, new ShareInfoVisitor(provUtil.getConsole()));
  }
}
