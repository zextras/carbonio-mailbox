package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.ProvUtil;

import java.util.Map;
import java.util.Set;

class GetDataSourcesCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetDataSourcesCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAccountDataSources(args);
  }

  private void doGetAccountDataSources(String[] args) throws ServiceException {
    Account account = provUtil.lookupAccount(args[1]);
    Set<String> attrNameSet = provUtil.getArgNameSet(args, 2);
    for (DataSource dataSource : provUtil.getProvisioning().getAllDataSources(account)) {
      dumpDataSource(dataSource, attrNameSet);
    }
  }

  public void dumpDataSource(DataSource dataSource, Set<String> argNameSet)
          throws ServiceException {
    var console = provUtil.getConsole();
    console.println("# name " + dataSource.getName());
    console.println("# type " + dataSource.getType());
    Map<String, Object> attrs = dataSource.getAttrs();
    dumper.dumpAttrs(attrs, argNameSet);
    console.println();
  }
}
