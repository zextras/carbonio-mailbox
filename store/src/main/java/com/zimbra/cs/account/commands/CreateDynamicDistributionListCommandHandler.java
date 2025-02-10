package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class CreateDynamicDistributionListCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateDynamicDistributionListCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.getConsole().println(provUtil.getProvisioning().createGroup(args[1], provUtil.getMapAndCheck(args, 2, true), true).getId());
  }
}
