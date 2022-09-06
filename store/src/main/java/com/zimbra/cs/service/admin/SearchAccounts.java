// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.account.SearchDirectoryOptions.SortOpt;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;
import com.zimbra.cs.session.AdminSession;
import com.zimbra.cs.session.Session;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class SearchAccounts extends AdminDocumentHandler {

  /** must be careful and only allow access to domain if domain admin */
  @Override
  public boolean domainAuthSufficient(Map context) {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    String query = request.getAttribute(AdminConstants.E_QUERY);

    int limit = (int) request.getAttributeLong(AdminConstants.A_LIMIT, Integer.MAX_VALUE);
    if (limit == 0) limit = Integer.MAX_VALUE;
    int offset = (int) request.getAttributeLong(AdminConstants.A_OFFSET, 0);
    String domain = request.getAttribute(AdminConstants.A_DOMAIN, null);
    boolean applyCos = request.getAttributeBool(AdminConstants.A_APPLY_COS, true);
    String attrsStr = request.getAttribute(AdminConstants.A_ATTRS, null);
    String sortBy = request.getAttribute(AdminConstants.A_SORT_BY, null);
    String types = request.getAttribute(AdminConstants.A_TYPES, "accounts");
    boolean sortAscending = request.getAttributeBool(AdminConstants.A_SORT_ASCENDING, true);

    String[] attrs = attrsStr == null ? null : attrsStr.split(",");

    // if we are a domain admin only, restrict to domain
    //
    // Note: isDomainAdminOnly *always* returns false for pure ACL based AccessManager
    if (isDomainAdminOnly(zsc)) {
      if (domain == null) {
        domain = getAuthTokenAccountDomain(zsc).getName();
      } else {
        checkDomainRight(zsc, domain, AdminRight.PR_ALWAYS_ALLOW);
      }
    }

    Domain d = null;
    if (domain != null) {
      d = prov.get(Key.DomainBy.name, domain);
      if (d == null) throw AccountServiceException.NO_SUCH_DOMAIN(domain);
    }

    AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);
    AdminAccessControl.SearchDirectoryRightChecker rightChecker =
        new AdminAccessControl.SearchDirectoryRightChecker(aac, prov, null);

    SearchDirectoryOptions searchOpts = new SearchDirectoryOptions(d, attrs);
    searchOpts.setTypes(types);
    searchOpts.setSortOpt(sortAscending ? SortOpt.SORT_ASCENDING : SortOpt.SORT_DESCENDING);
    searchOpts.setSortAttr(sortBy);
    searchOpts.setFilterString(FilterId.ADMIN_SEARCH, query);

    List<NamedEntry> accounts;
    int limitMax = offset + limit;
    AdminSession session = (AdminSession) getSession(zsc, Session.Type.ADMIN);
    if (session != null) {
      accounts = session.searchDirectory(searchOpts, offset, rightChecker);
    } else {
      accounts = prov.searchDirectory(searchOpts);
      accounts = rightChecker.getAllowed(accounts, limitMax);
    }

    Element response = zsc.createElement(AdminConstants.SEARCH_ACCOUNTS_RESPONSE);
    int numAccounts;
    for (numAccounts = offset;
        numAccounts < limitMax && numAccounts < accounts.size();
        numAccounts++) {
      NamedEntry entry = accounts.get(numAccounts);
      SearchDirectory.encodeEntry(prov, response, entry, applyCos, null, aac);
    }

    response.addAttribute(AdminConstants.A_MORE, numAccounts < accounts.size());
    response.addAttribute(AdminConstants.A_SEARCH_TOTAL, accounts.size());
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_getAccount);
    relatedRights.add(Admin.R_getCalendarResource);
    relatedRights.add(Admin.R_getDistributionList);
    relatedRights.add(Admin.R_getDomain);
    relatedRights.add(Admin.R_listAccount);
    relatedRights.add(Admin.R_listCalendarResource);
    relatedRights.add(Admin.R_listDistributionList);
    relatedRights.add(Admin.R_listDomain);

    notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
  }
}
