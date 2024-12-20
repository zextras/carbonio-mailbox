package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class AddDistributionListAliasCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public AddDistributionListAliasCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().addGroupAlias(provUtil.lookupGroup(args[1]), args[2]);
  }
}
