package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountLoggerOptions;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;

public class AddAccountLoggerCommandHandler implements CommandHandler {
  final ProvUtil provUtil;

  public AddAccountLoggerCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    var alo = AccountLoggerOptions.parseAccountLoggerOptions(args);
    doAddAccountLogger(alo);
  }

  private void doAddAccountLogger(AccountLoggerOptions alo) throws ServiceException {
    Provisioning prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = provUtil.lookupAccount(alo.args[1]);
    sp.addAccountLogger(acct, alo.args[2], alo.args[3], alo.server);
  }
}
