package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class DeleteDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().deleteDomain(provUtil.lookupDomain(args[1]).getId());
  }
}
