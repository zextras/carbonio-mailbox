package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class DeleteDataSourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteDataSourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    var account = provUtil.lookupAccount(args[1]);
    provUtil.getProvisioning().deleteDataSource(account, provUtil.lookupDataSourceId(account, args[2]));
  }
}
