package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class RenameCalendarResourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RenameCalendarResourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().renameCalendarResource(provUtil.lookupCalendarResource(args[1]).getId(), args[2]);
  }
}
