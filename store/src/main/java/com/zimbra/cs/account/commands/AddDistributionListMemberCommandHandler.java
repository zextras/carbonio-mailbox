package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class AddDistributionListMemberCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public AddDistributionListMemberCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doAddMember(args);
  }

  private void doAddMember(String[] args) throws ServiceException {
    String[] members = new String[args.length - 2];
    System.arraycopy(args, 2, members, 0, args.length - 2);
    provUtil.getProvisioning().addGroupMembers(provUtil.lookupGroup(args[1]), members);
  }
}
