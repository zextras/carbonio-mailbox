package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class AutoProvControlCommandHandler implements CommandHandler {
  final ProvUtil provUtil;

  public AutoProvControlCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().autoProvControl(args[1]);
  }
}
