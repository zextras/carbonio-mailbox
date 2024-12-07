package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class GetAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAccountCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAccount(args);
  }

  private void doGetAccount(String[] args) throws ServiceException {
    boolean applyDefault = true;
    int acctPos = 1;

    if (args[1].equals("-e")) {
      if (args.length > 1) {
        applyDefault = false;
        acctPos = 2;
      } else {
        provUtil.usage();
        return;
      }
    }

    provUtil.dumpAccount(
            provUtil.lookupAccount(args[acctPos], true, applyDefault),
            applyDefault,
            provUtil.getArgNameSet(args, acctPos + 1));
  }
}
