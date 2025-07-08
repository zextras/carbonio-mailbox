package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.soap.type.GalSearchType;

import java.util.Map;

class SearchGalCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public SearchGalCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, UsageException {
    doSearchGal(args);
  }

  private void doSearchGal(String[] args) throws ServiceException, ArgException, UsageException {
    if (args.length < 3) {
      provUtil.usage();
      return;
    }
    String domain = args[1];
    String query = args[2];
    Map<String, Object> attrs = provUtil.getMap(args, 3);
    String limitStr = (String) attrs.get("limit");
    int limit = limitStr == null ? 0 : Integer.parseInt(limitStr);
    String offsetStr = (String) attrs.get("offset");
    int offset = offsetStr == null ? 0 : Integer.parseInt(offsetStr);
    String sortBy = (String) attrs.get("sortBy");
    Domain d = provUtil.lookupDomain(domain);

    var prov = provUtil.getProvisioning();
    Provisioning.SearchGalResult result;

    if (prov instanceof LdapProv) {
      if (offsetStr != null) {
        throw ServiceException.INVALID_REQUEST("offset is not supported with -l", null);
      }

      if (sortBy != null) {
        throw ServiceException.INVALID_REQUEST("sortBy is not supported with -l", null);
      }

      GalContact.Visitor visitor =
              dumper::dumpContact;
      result = prov.searchGal(d, query, GalSearchType.all, limit, visitor);

    } else {
      result =
              ((SoapProvisioning) prov)
                      .searchGal(d, query, GalSearchType.all, null, limit, offset, sortBy);
      for (GalContact contact : result.getMatches()) {
        dumper.dumpContact(contact);
      }
    }
  }
}
