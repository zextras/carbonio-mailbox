package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class CreateServerCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateServerCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.getConsole().println(provUtil.getProvisioning().createServer(args[1], provUtil.getMapAndCheck(args, 2, true)).getId());
  }
}
