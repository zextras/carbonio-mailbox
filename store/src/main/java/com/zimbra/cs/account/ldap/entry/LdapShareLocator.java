// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ShareLocator;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

public class LdapShareLocator extends ShareLocator implements LdapEntry {

  private String mDn;

  public LdapShareLocator(String dn, ZAttributes attrs, Provisioning prov) throws LdapException {
    super(attrs.getAttrString(Provisioning.A_cn), attrs.getAttrs(), prov);
    mDn = dn;
  }

  @Override
  public String getDN() {
    return mDn;
  }
}
