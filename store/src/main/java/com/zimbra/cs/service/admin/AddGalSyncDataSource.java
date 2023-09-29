// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AddGalSyncDataSourceRequest;
import com.zimbra.soap.admin.type.GalMode;
import com.zimbra.soap.type.AccountBy;
import com.zimbra.soap.type.AccountSelector;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddGalSyncDataSource extends AdminDocumentHandler {

  @Override
  public boolean domainAuthSufficient(Map<String, Object> context) {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    AddGalSyncDataSourceRequest dsRequest = zsc.elementToJaxb(request);

    String name = dsRequest.getName();
    String domainStr = dsRequest.getDomain();
    GalMode type = dsRequest.getType();

    AccountSelector acctSelector = dsRequest.getAccount();
    AccountBy acctBy = acctSelector.getBy();
    String acctValue = acctSelector.getKey();
    String folder = dsRequest.getFolder();

    Domain domain = prov.getDomainByName(domainStr);

    if (domain == null) {
      throw AccountServiceException.NO_SUCH_DOMAIN(domainStr);
    }

    Account account = null;
    try {
      account = prov.get(acctBy.toKeyAccountBy(), acctValue);
    } catch (ServiceException se) {
      ZimbraLog.gal.warn("error checking GalSyncAccount", se);
    }

    if (account == null) {
      throw AccountServiceException.NO_SUCH_ACCOUNT(acctValue);
    }

    if (!Provisioning.getInstance().onLocalServer(account)) {
      String host = account.getMailHost();
      Server server = prov.getServerByName(host);
      return proxyRequest(request, context, server);
    }

    CreateGalSyncAccount.addDataSource(request, zsc, account, domain, folder, name, type);

    Element response = zsc.createElement(AdminConstants.ADD_GAL_SYNC_DATASOURCE_RESPONSE);
    ToXML.encodeAccount(response, account, false, emptySet, null);
    return response;
  }

  private static final Set<String> emptySet = Collections.emptySet();

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    // XXX revisit
    relatedRights.add(Admin.R_createAccount);
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY, Admin.R_modifyAccount.getName(), "account"));
  }
}
