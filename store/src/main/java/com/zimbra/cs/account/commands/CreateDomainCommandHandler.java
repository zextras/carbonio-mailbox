package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.common.cli.CommandExitException;
import com.zimbra.cs.service.admin.DomainUtils;

class CreateDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override
  public void handle(String[] args) throws ServiceException, ArgException, CommandExitException {
    var provisioning = provUtil.getProvisioning();
    var attributes = provUtil.getMapAndCheck(args, 2, true);
    
    var domain = provisioning.createDomain(args[1], attributes);

    var virtualHostnames = DomainUtils.getVirtualHostnamesFromAttributes(attributes);
    var conflictingDomains = DomainUtils.getDomainsWithConflictingVHosts(domain, virtualHostnames, provisioning);

    var console = provUtil.getConsole();
    console.println(domain.getId());
    if (!conflictingDomains.isEmpty()) {
      console.println(DomainUtils.getDuplicateVirtualHostnameWarningMessage(domain, conflictingDomains));
    }
  }
}
