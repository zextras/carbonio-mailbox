package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.common.cli.CommandExitException;

class CreateDistributionListCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateDistributionListCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, CommandExitException {
    provUtil.getConsole().println(provUtil.getProvisioning().createGroup(args[1], provUtil.getMapAndCheck(args, 2, true), false).getId());
  }
}
