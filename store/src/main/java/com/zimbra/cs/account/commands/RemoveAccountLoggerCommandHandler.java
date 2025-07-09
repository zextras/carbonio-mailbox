package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.InvalidCommandException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Command;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

class RemoveAccountLoggerCommandHandler implements CommandHandler {
  private static final Command command = Command.REMOVE_ACCOUNT_LOGGER;
  private final ProvUtil provUtil;

  public RemoveAccountLoggerCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, InvalidCommandException {
    var alo = AccountLoggerOptions.parseAccountLoggerOptions(args);
    if (!command.checkArgsLength(alo.args)) {
      provUtil.usage();
      return;
    }
    doRemoveAccountLogger(alo);
  }

  private void doRemoveAccountLogger(AccountLoggerOptions alo) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = null;
    String category = null;
    if (alo.args.length == 2) {
      // Hack: determine if it's an account or category, based on the name.
      String arg = alo.args[1];
      if (arg.startsWith("zimbra.") || arg.startsWith("com.zimbra")) {
        category = arg;
      } else {
        acct = provUtil.lookupAccount(alo.args[1]);
      }
    }
    if (alo.args.length == 3) {
      acct = provUtil.lookupAccount(alo.args[1]);
      category = alo.args[2];
    }
    sp.removeAccountLoggers(acct, category, alo.server);
  }
}
