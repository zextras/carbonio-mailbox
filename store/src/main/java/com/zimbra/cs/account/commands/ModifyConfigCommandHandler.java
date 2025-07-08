package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ProvUtil.Exit2Exception;

class ModifyConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyConfigCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, Exit2Exception {
    var prov = provUtil.getProvisioning();
    prov.modifyAttrs(prov.getConfig(), provUtil.getMapAndCheck(args, 1, false), true);
  }
}
