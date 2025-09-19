package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class DeleteSignatureCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteSignatureCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    var account = provUtil.lookupAccount(args[1]);
    provUtil.getProvisioning().deleteSignature(account, provUtil.lookupSignatureId(account, args[2]));
  }
}
