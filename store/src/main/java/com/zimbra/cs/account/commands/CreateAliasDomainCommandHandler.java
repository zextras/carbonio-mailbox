package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.common.cli.CommandExitException;
import com.zimbra.cs.account.Provisioning;

import java.util.Map;

class CreateAliasDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateAliasDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, CommandExitException {
    provUtil.getConsole().println(
            doCreateAliasDomain(args[1], args[2], provUtil.getMapAndCheck(args, 3, true)).getId());
  }

  private Domain doCreateAliasDomain(
          String aliasDomain, String localDomain, Map<String, Object> attrs) throws ServiceException {
    Domain local = provUtil.lookupDomain(localDomain);
    if (!local.isLocal()) {
      throw ServiceException.INVALID_REQUEST("target domain must be a local domain", null);
    }
    attrs.put(ZAttrProvisioning.A_zimbraDomainType, Provisioning.DomainType.alias.name());
    attrs.put(ZAttrProvisioning.A_zimbraDomainAliasTargetId, local.getId());
    return provUtil.getProvisioning().createDomain(aliasDomain, attrs);
  }
}
