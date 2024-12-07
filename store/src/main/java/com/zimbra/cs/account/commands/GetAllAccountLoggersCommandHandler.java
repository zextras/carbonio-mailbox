package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.AccountLogger;
import com.zimbra.cs.account.AccountLoggerOptions;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

import java.util.List;
import java.util.Map;

public class GetAllAccountLoggersCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllAccountLoggersCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    var alo = AccountLoggerOptions.parseAccountLoggerOptions(args);
    doGetAllAccountLoggers(alo);
  }

  private void doGetAllAccountLoggers(AccountLoggerOptions alo) throws ServiceException {
    if (!(provUtil.getProvisioning() instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) provUtil.getProvisioning();

    var console = provUtil.getConsole();
    Map<String, List<AccountLogger>> allLoggers = sp.getAllAccountLoggers(alo.server);
    for (String accountName : allLoggers.keySet()) {
      console.println(String.format("# name %s", accountName));
      for (AccountLogger logger : allLoggers.get(accountName)) {
        console.println(String.format("%s=%s", logger.getCategory(), logger.getLevel()));
      }
    }
  }
}
