// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapIdentity extends Identity implements LdapEntry {

    private String mDn;

    public LdapIdentity(Account acct, String dn, ZAttributes attrs, Provisioning prov) throws LdapException {
        super(acct, attrs.getAttrString(Provisioning.A_zimbraPrefIdentityName),
                attrs.getAttrString(Provisioning.A_zimbraPrefIdentityId),
                attrs.getAttrs(), prov);
        mDn = dn;
    }

    public String getDN() {
        return mDn;
    }

}
