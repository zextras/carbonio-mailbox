package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.fb.FbCli;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class PushFreebusyCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public PushFreebusyCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, HttpException, IOException {
    doPushFreeBusy(args);
  }

  private void doPushFreeBusy(String[] args) throws ServiceException, IOException, HttpException {
    FbCli fbcli = new FbCli();
    Map<String, HashSet<String>> accountMap = new HashMap<>();
    var prov = provUtil.getProvisioning();
    var console = provUtil.getConsole();
    for (int i = 1; i < args.length; i++) {
      String acct = args[i];
      Account account = prov.getAccountById(acct);
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(acct);
      }
      String host = account.getMailHost();
      HashSet<String> accountSet = accountMap.get(host);
      if (accountSet == null) {
        accountSet = new HashSet<>();
        accountMap.put(host, accountSet);
      }
      accountSet.add(acct);
    }
    for (String host : accountMap.keySet()) {
      console.println("pushing to server " + host);
      fbcli.setServer(host);
      fbcli.pushFreeBusyForAccounts(accountMap.get(host));
    }
  }
}
