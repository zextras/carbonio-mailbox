package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ldap.LdapProv;
import org.apache.http.HttpException;

import java.io.IOException;

public class RenameDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RenameDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doRenameDomain(args);
  }

  private void doRenameDomain(String[] args) throws ServiceException {

    var prov = provUtil.getProvisioning();
    // bug 56768
    // if we are not already using master only, force it to use master.
    // Note: after rename domain, the zmprov instance will stay in "master only" mode.
    if (!provUtil.getUseLdapMaster()) {
      ((LdapProv) prov).alwaysUseMaster();
    }
    var console = provUtil.getConsole();

    LdapProv lp = (LdapProv) prov;
    Domain domain = provUtil.lookupDomain(args[1]);
    lp.renameDomain(domain.getId(), args[2]);
    console.printOutput("domain " + args[1] + " renamed to " + args[2]);
    console.printOutput(
            "Note: use zmlocalconfig to check and update any localconfig settings referencing"
                    + " domain '"
                    + args[1]
                    + "' on all servers.");
    console.printOutput(
            "Use /opt/zextras/libexec/zmdkimkeyutil to recreate the DKIM entries for new domain"
                    + " name if required.");
  }

}
