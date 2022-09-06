// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.gal.ZimbraGalSearchBase.PredefinedSearchBase;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import java.util.Map;

/**
 * @author pshao
 */
public class LdapDomain extends Domain implements LdapEntry {

  private String mDn;

  public LdapDomain(String dn, ZAttributes attrs, Map<String, Object> defaults, Provisioning prov)
      throws LdapException {
    super(
        attrs.getAttrString(Provisioning.A_zimbraDomainName),
        attrs.getAttrString(Provisioning.A_zimbraId),
        attrs.getAttrs(),
        defaults,
        prov);
    mDn = dn;
  }

  public String getDN() {
    return mDn;
  }

  @Override
  public String getGalSearchBase(String searchBaseRaw) throws ServiceException {
    LdapProv ldapProv = (LdapProv) getProvisioning();

    if (searchBaseRaw.equalsIgnoreCase(PredefinedSearchBase.DOMAIN.name())) {
      // dynamic groups are under the cn=groups tree,
      // accounts and Dls are under the people tree
      // We can no longer just search under the people tree because that
      // will leave dynamic groups out.   We don't want to do two(once under the
      // people tree, once under the groups tree) LDAP searches either because
      // that will hurt perf.
      // As of bug 66001, we now use the dnSubtreeMatch filter
      // (extension supported by OpenLDAP) to exclude entries in sub domains.
      // See getDnSubtreeMatchFilter().
      return getDN();
      // return ldapProv.getDIT().domainDNToAccountSearchDN(getDN());
    } else if (searchBaseRaw.equalsIgnoreCase(PredefinedSearchBase.SUBDOMAINS.name())) {
      return getDN();
    } else if (searchBaseRaw.equalsIgnoreCase(PredefinedSearchBase.ROOT.name())) {
      return LdapConstants.DN_ROOT_DSE;
    }

    // broken by p4 changed 150971, fixed now
    return searchBaseRaw;
  }

  public ZLdapFilter getDnSubtreeMatchFilter() throws ServiceException {
    LdapProv ldapProv = (LdapProv) getProvisioning();

    return ZLdapFilterFactory.getInstance()
        .dnSubtreeMatch(
            ldapProv.getDIT().domainDNToAccountSearchDN(getDN()),
            ldapProv.getDIT().domainDNToDynamicGroupsBaseDN(getDN()));
  }
}
