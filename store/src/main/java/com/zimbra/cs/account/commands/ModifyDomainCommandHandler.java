package com.zimbra.cs.account.commands;

import com.zimbra.common.cli.CommandExitException;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.service.admin.DomainUtils;


class ModifyDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override
  public void handle(String[] args) throws ServiceException, ArgException, CommandExitException {
    var provisioning = provUtil.getProvisioning();
    var attributes = provUtil.getMapAndCheck(args, 2, false);
    var domain = provUtil.lookupDomain(args[1]);

    provisioning.modifyAttrs(domain, attributes, true);

    var virtualHostnames = DomainUtils.getVirtualHostnamesFromAttributes(attributes);
    var conflictingDomains = DomainUtils.getDomainsWithConflictingVHosts(domain, virtualHostnames, provisioning);
    if (!conflictingDomains.isEmpty()) {
      provUtil.getConsole().println(DomainUtils.getDuplicateVirtualHostnameWarningMessage(domain, conflictingDomains));
    }
  }
}
