package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class CopyCosCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CopyCosCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    String id = provUtil.getProvisioning().copyCos(provUtil.lookupCos(args[1]).getId(), args[2]).getId();
    provUtil.getConsole().println(id);
  }
}
