package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.admin.type.DataSourceType;

class CreateDataSourceCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateDataSourceCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    provUtil.getConsole().println(
            provUtil.getProvisioning().createDataSource(
                            provUtil.lookupAccount(args[1]),
                            DataSourceType.fromString(args[2]),
                            args[3],
                            provUtil.getMapAndCheck(args, 4, true))
                    .getId());
  }
}
