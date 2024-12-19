package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetServerCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetServerCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetServer(args);
  }

  private void doGetServer(String[] args) throws ServiceException {
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
    dumper.dumpServer(provUtil.lookupServer(args[i], applyDefault), applyDefault, provUtil.getArgNameSet(args, i + 1));
  }

}
