package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ProvUtil.Exit2Exception;

class CreateCalendarResourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateCalendarResourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, Exit2Exception {
    provUtil.getConsole().println(
            provUtil.getProvisioning().createCalendarResource(
                            args[1], args[2].isEmpty() ? null : args[2], provUtil.getMapAndCheck(args, 3, true))
                    .getId());
  }
}
