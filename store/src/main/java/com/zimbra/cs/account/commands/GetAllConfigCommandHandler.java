package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

public class GetAllConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllConfigCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.dumpAttrs(provUtil.getProvisioning().getConfig().getAttrs(), provUtil.getArgNameSet(args, 1));
  }
}
