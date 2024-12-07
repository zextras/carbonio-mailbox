package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class AddAccountAliasCommandHandler implements CommandHandler {
  final ProvUtil provUtil;
  public AddAccountAliasCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().addAlias(provUtil.lookupAccount(args[1]), args[2]);
  }
}
