package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetDistributionListCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetDistributionListCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    provUtil.dumpGroup(provUtil.lookupGroup(args[1]), provUtil.getArgNameSet(args, 2));
  }
}
