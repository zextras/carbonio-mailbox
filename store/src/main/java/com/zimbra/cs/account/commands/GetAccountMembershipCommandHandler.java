package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Console;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.ProvUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

class GetAccountMembershipCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAccountMembershipCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAccountMembership(args);
  }

  private void doGetAccountMembership(String[] args) throws ServiceException {
    String key = null;
    boolean idsOnly = false;
    if (args.length > 2) {
      idsOnly = args[1].equals("-i");
      key = args[2];
    } else {
      key = args[1];
    }
    Account account = provUtil.lookupAccount(key);
    Console console = provUtil.getConsole();
    if (idsOnly) {
      Set<String> lists = provUtil.getProvisioning().getGroups(account);
      for (String id : lists) {
        console.println(id);
      }
    } else {
      HashMap<String, String> via = new HashMap<>();
      List<Group> groups = provUtil.getProvisioning().getGroups(account, false, via);
      for (Group group : groups) {
        String viaDl = via.get(group.getName());
        if (viaDl != null) {
          console.println(group.getName() + " (via " + viaDl + ")");
        } else {
          console.println(group.getName());
        }
      }
    }
  }

}
