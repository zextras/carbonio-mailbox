package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetDistributionListMembershipCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetDistributionListMembershipCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetDistributionListMembership(provUtil.lookupGroup(args[1]));
  }

  private void doGetDistributionListMembership(Group group) throws ServiceException {
    String[] members;
    if (group instanceof DynamicGroup dynamicGroup) {
      members = dynamicGroup.getAllMembers(true);
    } else {
      members = group.getAllMembers();
    }

    var console = provUtil.getConsole();
    int count = members == null ? 0 : members.length;
    console.println("# distributionList " + group.getName() + " memberCount=" + count);
    console.println();
    console.println("members");
    for (String member : members) {
      console.println(member);
    }
  }
}
