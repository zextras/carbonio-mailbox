package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class ExitCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ExitCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    System.exit(provUtil.getErrorOccursDuringInteraction() ? 2 : 0);
  }
}
