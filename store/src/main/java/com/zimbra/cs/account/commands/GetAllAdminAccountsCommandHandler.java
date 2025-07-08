package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;

import java.util.List;
import java.util.Set;

class GetAllAdminAccountsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllAdminAccountsCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, UsageException {
    doGetAllAdminAccounts(args);
  }

  private void doGetAllAdminAccounts(String[] args) throws ServiceException, UsageException {
    boolean verbose = false;
    boolean applyDefault = true;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else {
        break;
      }
      i++;
    }

    var console = provUtil.getConsole();
    if (!applyDefault && !verbose) {
      console.println(ProvUtil.ERR_INVALID_ARG_EV);
      provUtil.usageWithUsageException();
      return;
    }

    List<Account> accounts;
    Provisioning prov = provUtil.getProvisioning();
    if (prov instanceof SoapProvisioning soapProv) {
      accounts = soapProv.getAllAdminAccounts(applyDefault);
    } else {
      accounts = prov.getAllAdminAccounts();
    }
    Set<String> attrNames = provUtil.getArgNameSet(args, i);
    for (Account account : accounts) {
      if (verbose) {
        dumper.dumpAccount(account, applyDefault, attrNames);
      } else {
        console.println(account.getName());
      }
    }
  }

}
