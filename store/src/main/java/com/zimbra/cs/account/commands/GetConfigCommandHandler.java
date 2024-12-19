package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class GetConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetConfigCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetConfig(args);
  }

  private void doGetConfig(String[] args) throws ServiceException {
    String key = args[1];
    Set<String> needAttr = new HashSet<>();
    needAttr.add(key);
    dumper.dumpAttrs(provUtil.getProvisioning().getConfig(key).getAttrs(), needAttr);
  }
}
