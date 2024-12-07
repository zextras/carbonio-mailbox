package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.type.GalSearchType;

public class AutoCompleteGalCommandHandler implements CommandHandler {
  final ProvUtil provUtil;

  public AutoCompleteGalCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
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
            gc -> provUtil.dumpContact(gc);
    provUtil.getProvisioning().autoCompleteGal(d, query, GalSearchType.all, limit, visitor);
  }
}
