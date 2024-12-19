package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.type.GalSearchType;

class AutoCompleteGalCommandHandler implements CommandHandler {
  final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public AutoCompleteGalCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doAutoCompleteGal(args);
  }

  private void doAutoCompleteGal(String[] args) throws ServiceException {
    String domain = args[1];
    String query = args[2];
    int limit = 100;

    Domain d = provUtil.lookupDomain(domain);

    GalContact.Visitor visitor =
            dumper::dumpContact;
    provUtil.getProvisioning().autoCompleteGal(d, query, GalSearchType.all, limit, visitor);
  }
}
