package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class DeleteIdentityCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteIdentityCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().deleteIdentity(provUtil.lookupAccount(args[1]), args[2]);
  }
}
