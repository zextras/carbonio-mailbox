package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class GetCosCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetCosCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    dumper.dumpCos(provUtil.lookupCos(args[1]), provUtil.getArgNameSet(args, 2));
  }
}
