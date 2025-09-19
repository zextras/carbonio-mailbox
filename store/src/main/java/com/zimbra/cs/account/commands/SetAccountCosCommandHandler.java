package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class SetAccountCosCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public SetAccountCosCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().setCOS(provUtil.lookupAccount(args[1]), provUtil.lookupCos(args[2]));
  }
}
