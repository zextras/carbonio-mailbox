package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

class ChangePrimaryEmailCommandHandler implements CommandHandler {
  final ProvUtil provUtil;

  public ChangePrimaryEmailCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doChangePrimaryEmail(args);
  }

  private void doChangePrimaryEmail(String[] args) throws ServiceException {
    if (!(provUtil.getProvisioning() instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    ((SoapProvisioning) provUtil.getProvisioning()).changePrimaryEmail(provUtil.lookupAccount(args[1]).getId(), args[2]);
  }
}
