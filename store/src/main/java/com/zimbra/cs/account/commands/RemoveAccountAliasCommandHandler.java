package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class RemoveAccountAliasCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RemoveAccountAliasCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    Account acct = provUtil.lookupAccount(args[1], false);
    provUtil.getProvisioning().removeAlias(acct, args[2]);
    // even if acct is null, we still invoke removeAlias and throw an exception
    // afterwards.
    // this is so dangling aliases can be cleaned up as much as possible
    if (acct == null) {
      throw AccountServiceException.NO_SUCH_ACCOUNT(args[1]);
    }
  }
}
