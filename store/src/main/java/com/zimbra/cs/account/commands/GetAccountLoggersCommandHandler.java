package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.AccountLogger;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountLoggerOptions;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

public class GetAccountLoggersCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAccountLoggersCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    var alo = AccountLoggerOptions.parseAccountLoggerOptions(args);
    doGetAccountLoggers(alo);
  }

  private void doGetAccountLoggers(AccountLoggerOptions alo) throws ServiceException {
    if (!(provUtil.getProvisioning() instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) provUtil.getProvisioning();
    Account acct = provUtil.lookupAccount(alo.args[1]);
    for (AccountLogger accountLogger : sp.getAccountLoggers(acct, alo.server)) {
      provUtil.getConsole().println(String.format("%s=%s", accountLogger.getCategory(), accountLogger.getLevel()));
    }
  }
}
