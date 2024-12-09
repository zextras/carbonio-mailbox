package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class CreateAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateAccountCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.getConsole().println(
            provUtil.getProvisioning().createAccount(
                            args[1], args[2].equals("") ? null : args[2], provUtil.getMapAndCheck(args, 3, true))
                    .getId());
  }
}
