// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.GlobalGrant;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapGlobalGrant extends GlobalGrant implements LdapEntry {
    
    private String mDn;
    
    public LdapGlobalGrant(String dn, ZAttributes attrs, Provisioning provisioning) throws LdapException {
        super(attrs.getAttrs(), provisioning);
        mDn = dn;
    }

    public String getDN() {
        return mDn;
    }
}