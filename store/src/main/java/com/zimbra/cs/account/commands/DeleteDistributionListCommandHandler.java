package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class DeleteDistributionListCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteDistributionListCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doDeleteDistributionList(args);
  }

  private void doDeleteDistributionList(String[] args) throws ServiceException {
    String groupId = provUtil.lookupGroup(args[1]).getId();
    boolean cascadeDelete = false;
    if (args.length > 2) {
      cascadeDelete = Boolean.parseBoolean(args[2]);
    }
    provUtil.getProvisioning().deleteGroup(groupId, cascadeDelete);
  }
}

