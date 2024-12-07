package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;

import java.util.Map;

public class CreateXMPPComponentCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateXMPPComponentCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doCreateXMPPComponent(args);
  }

  private void doCreateXMPPComponent(String[] args) throws ServiceException, ArgException {
    // 4 = class
    // 5 = category
    // 6 = type
    Map<String, Object> map = provUtil.getMapAndCheck(args, 7, true);
    map.put(Provisioning.A_zimbraXMPPComponentClassName, args[4]);
    map.put(Provisioning.A_zimbraXMPPComponentCategory, args[5]);
    map.put(Provisioning.A_zimbraXMPPComponentType, args[6]);
    Domain d = provUtil.lookupDomain(args[2]);
    String routableName = args[1] + "." + d.getName();
    provUtil.getConsole().println(
            provUtil.getProvisioning().createXMPPComponent(routableName, provUtil.lookupDomain(args[2]), provUtil.lookupServer(args[3]), map));
  }
}
