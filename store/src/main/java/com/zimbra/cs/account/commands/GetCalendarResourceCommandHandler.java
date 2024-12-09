package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetCalendarResourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetCalendarResourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    provUtil.dumpCalendarResource(provUtil.lookupCalendarResource(args[1]), true, provUtil.getArgNameSet(args, 2));
  }
}
