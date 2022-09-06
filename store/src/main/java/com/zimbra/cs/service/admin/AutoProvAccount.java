// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AutoProvAccountRequest;
import com.zimbra.soap.type.AutoProvPrincipalBy;
import java.util.List;
import java.util.Map;

public class AutoProvAccount extends AdminDocumentHandler {

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   *     delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    AutoProvAccountRequest req = zsc.elementToJaxb(request);
    DomainBy domainBy = req.getDomain().getBy().toKeyDomainBy();
    String domainKey = req.getDomain().getKey();
    Domain domain = prov.get(domainBy, domainKey);
    if (domain == null) {
      throw AccountServiceException.NO_SUCH_DOMAIN(domainKey);
    }

    checkRight(zsc, context, domain, Admin.R_autoProvisionAccount);

    AutoProvPrincipalBy by = req.getPrincipal().getBy();
    String principal = req.getPrincipal().getKey();

    String password = req.getPassword();

    Account acct = prov.autoProvAccountManual(domain, by, principal, password);
    if (acct == null) {
      throw ServiceException.FAILURE("unable to auto provision account: " + principal, null);
    }

    Element response = zsc.createElement(AdminConstants.AUTO_PROV_ACCOUNT_RESPONSE);
    ToXML.encodeAccount(response, acct);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_autoProvisionAccount);
  }
}
