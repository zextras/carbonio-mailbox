package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class RenameDistributionListCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RenameDistributionListCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().renameGroup(provUtil.lookupGroup(args[1]).getId(), args[2]);
  }
}
