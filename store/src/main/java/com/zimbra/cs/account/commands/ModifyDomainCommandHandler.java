package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.service.admin.ModifyDomain;


class ModifyDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override 
  public void handle(String[] args) throws ServiceException, ArgException {
    var provisioning = provUtil.getProvisioning();
    var domain = provUtil.lookupDomain(args[1]);
    var attributes = provUtil.getMapAndCheck(args, 2, false);
    var virtualHostnames = ModifyDomain.getVirtualHostnamesFromAttributes(attributes);
    var conflictingDomains = ModifyDomain.getConflictingDomains(domain, virtualHostnames, provisioning);

    provisioning.modifyAttrs(domain, attributes, true);

    if (!conflictingDomains.isEmpty()) {
      provUtil.getConsole().println(
              "Virtual hostname modification for domain '" + domain.getName() +
                      "' conflicts with existing virtual hostnames in domains: " +
                      String.join(", ", conflictingDomains) + ". This may cause routing issues.");
    }
  }
}
