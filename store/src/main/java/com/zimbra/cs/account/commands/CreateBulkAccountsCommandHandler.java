package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class CreateBulkAccountsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateBulkAccountsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doCreateAccountsBulk(args);
  }

  private void doCreateAccountsBulk(String[] args) throws ServiceException {
    String domain = args[1];
    String userPassword =  args[4];
    String nameMask = args[2];
    int numAccounts = Integer.parseInt(args[3]);
    for (int ix = 0; ix < numAccounts; ix++) {
      String name = nameMask + ix + "@" + domain;
      Map<String, Object> attrs = new HashMap<>();
      String displayName = nameMask + " N. " + ix;
      StringUtil.addToMultiMap(attrs, "displayName", displayName);
      Account createdAccount = provUtil.getProvisioning().createAccount(name, userPassword, attrs);
      provUtil.getConsole().println(createdAccount.getId());
    }
  }
}
