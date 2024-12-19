package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetCalendarResourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetCalendarResourceCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    dumper.dumpCalendarResource(provUtil.lookupCalendarResource(args[1]), true, provUtil.getArgNameSet(args, 2));
  }
}
