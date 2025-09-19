package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class DeleteServerCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteServerCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().deleteServer(provUtil.lookupServer(args[1]).getId());
  }
}
