package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.soap.type.GalSearchType;

class SyncGalCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public SyncGalCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doSyncGal(args);
  }

  private void doSyncGal(String[] args) throws ServiceException {
    String domain = args[1];
    String token = args.length == 3 ? args[2] : "";

    Domain d = provUtil.lookupDomain(domain);

    var prov = provUtil.getProvisioning();
    Provisioning.SearchGalResult result = null;
    if (prov instanceof LdapProv) {
      GalContact.Visitor visitor =
              dumper::dumpContact;
      result = prov.syncGal(d, token, visitor);
    } else {
      result = ((SoapProvisioning) prov).searchGal(d, "", GalSearchType.all, token, 0, 0, null);
      for (GalContact contact : result.getMatches()) {
        dumper.dumpContact(contact);
      }
    }

    if (result.getToken() != null) {
      provUtil.getConsole().println("\n# token = " + result.getToken() + "\n");
    }
  }
}
