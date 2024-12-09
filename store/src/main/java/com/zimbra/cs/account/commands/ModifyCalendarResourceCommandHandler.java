package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

public class ModifyCalendarResourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyCalendarResourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    provUtil.getProvisioning().modifyAttrs(provUtil.lookupCalendarResource(args[1]), provUtil.getMapAndCheck(args, 2, false), true);
  }
}
