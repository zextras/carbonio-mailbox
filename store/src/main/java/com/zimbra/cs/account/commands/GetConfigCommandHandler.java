package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GetConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetConfigCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetConfig(args);
  }

  private void doGetConfig(String[] args) throws ServiceException {
    String key = args[1];
    Set<String> needAttr = new HashSet<>();
    needAttr.add(key);
    provUtil.dumpAttrs(provUtil.getProvisioning().getConfig(key).getAttrs(), needAttr);
  }
}
