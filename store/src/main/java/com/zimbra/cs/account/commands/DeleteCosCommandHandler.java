package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class DeleteCosCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteCosCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().deleteCos(provUtil.lookupCos(args[1]).getId());
  }
}
