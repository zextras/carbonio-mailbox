package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class RemoveDistributionListMemberCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RemoveDistributionListMemberCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doRemoveMember(args);
  }

  private void doRemoveMember(String[] args) throws ServiceException {
    String[] members = new String[args.length - 2];
    System.arraycopy(args, 2, members, 0, args.length - 2);
    provUtil.getProvisioning().removeGroupMembers(provUtil.lookupGroup(args[1]), members);
  }
}
