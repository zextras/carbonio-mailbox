// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.property.Acl;
import com.zimbra.cs.dav.property.CalDavProperty;
import com.zimbra.cs.dav.property.CardDavProperty;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.QName;

public class User extends Principal {

  public User(DavContext ctxt, Account account, String url) throws ServiceException {
    super(account, url);
    setAccount(account);
    String user = getOwner();
    addProperty(CalDavProperty.getCalendarHomeSet(user));
    addProperty(CalDavProperty.getCalendarUserType(this));
    addProperty(CalDavProperty.getScheduleInboxURL(user));
    addProperty(CalDavProperty.getScheduleOutboxURL(user));
    if (ctxt.getAuthAccount().equals(account)) {
      if (ctxt.useIcalDelegation()) {
        addProperty(new CalendarProxyReadFor(getAccount()));
        addProperty(new CalendarProxyWriteFor(getAccount()));
        addProperty(new ProxyGroupMembership(getAccount()));
      }
    }
    addProperty(Acl.getPrincipalUrl(this));
    ArrayList<String> addrs = new ArrayList<String>();
    for (String addr : account.getMailDeliveryAddress()) addrs.add(addr);
    for (String alias : account.getMailAlias()) addrs.add(alias);
    String principalAddr = UrlNamespace.getPrincipalUrl(account);
    if (principalAddr.endsWith("/")) {
      principalAddr = principalAddr.substring(0, principalAddr.length() - 1);
    }
    addrs.add(principalAddr);
    addProperty(CalDavProperty.getCalendarUserAddressSet(addrs));
    addProperty(CardDavProperty.getAddressbookHomeSet(user));
    setProperty(DavElements.E_HREF, url);
    String cn = account.getAttr(Provisioning.A_cn);
    if (cn == null) cn = account.getName();
    setProperty(DavElements.E_DISPLAYNAME, cn);
    mUri = url;
  }

  @Override
  public java.util.Collection<DavResource> getChildren(DavContext ctxt) throws DavException {
    ArrayList<DavResource> proxies = new ArrayList<DavResource>();
    if (ctxt.useIcalDelegation()) {
      try {
        proxies.add(new CalendarProxyRead(getOwner(), mUri));
        proxies.add(new CalendarProxyWrite(getOwner(), mUri));
      } catch (ServiceException e) {
      }
    }
    return proxies;
  }

  @Override
  public void delete(DavContext ctxt) throws DavException {
    throw new DavException("cannot delete this resource", HttpServletResponse.SC_FORBIDDEN, null);
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  private static QName[] SUPPORTED_REPORTS = {
    DavElements.E_ACL_PRINCIPAL_PROP_SET,
    DavElements.E_PRINCIPAL_MATCH,
    DavElements.E_PRINCIPAL_PROPERTY_SEARCH,
    DavElements.E_PRINCIPAL_SEARCH_PROPERTY_SET,
    DavElements.E_EXPAND_PROPERTY
  };

  @Override
  protected QName[] getSupportedReports() {
    return SUPPORTED_REPORTS;
  }
}
