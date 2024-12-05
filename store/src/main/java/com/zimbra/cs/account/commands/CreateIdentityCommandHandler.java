package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class CreateIdentityCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateIdentityCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.getProvisioning().createIdentity(provUtil.lookupAccount(args[1]), args[2], provUtil.getMapAndCheck(args, 3, true));
  }
}
