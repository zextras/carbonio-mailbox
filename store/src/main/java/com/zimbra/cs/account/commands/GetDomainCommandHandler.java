package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class GetDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetDomainCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, UsageException {
    doGetDomain(args);
  }

  private void doGetDomain(String[] args) throws ServiceException, UsageException {
    boolean applyDefault = true;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-e")) {
        applyDefault = false;
      } else {
        break;
      }
      i++;
    }
    if (i >= args.length) {
      provUtil.usage();
      return;
    }
    dumper.dumpDomain(provUtil.lookupDomain(args[i], provUtil.getProvisioning(), applyDefault), applyDefault, provUtil.getArgNameSet(args, i + 1));
  }

}
