package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.common.cli.ExitCodeException;

class ModifyServerCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyServerCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, ExitCodeException {
    provUtil.getProvisioning().modifyAttrs(provUtil.lookupServer(args[1]), provUtil.getMapAndCheck(args, 2, false), true);
  }
}
