package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.ProvUtil;

import java.util.Set;

class GetIdentitiesCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetIdentitiesCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAccountIdentities(args);
  }

  private void doGetAccountIdentities(String[] args) throws ServiceException {
    Account account = provUtil.lookupAccount(args[1]);
    Set<String> argNameSet = provUtil.getArgNameSet(args, 2);
    for (Identity identity : provUtil.getProvisioning().getAllIdentities(account)) {
      provUtil.dumpIdentity(identity, argNameSet);
    }
  }

}
