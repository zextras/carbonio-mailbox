// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;
import java.util.Map;

/**
 * @author pshao
 */
public class LdapCalendarResource extends CalendarResource implements LdapEntry {

  private String mDn;

  public LdapCalendarResource(
      String dn, String email, ZAttributes attrs, Map<String, Object> defaults, Provisioning prov)
      throws LdapException {
    super(email, attrs.getAttrString(Provisioning.A_zimbraId), attrs.getAttrs(), defaults, prov);
    mDn = dn;
  }

  public String getDN() {
    return mDn;
  }
}
