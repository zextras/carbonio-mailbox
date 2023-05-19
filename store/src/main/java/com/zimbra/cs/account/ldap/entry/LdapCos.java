// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapCos extends Cos implements LdapEntry {

    private final String mDn;
    
    public LdapCos(String dn, ZAttributes attrs, Provisioning prov) throws LdapException {
        super(attrs.getAttrString(Provisioning.A_cn), attrs.getAttrString(Provisioning.A_zimbraId), 
                attrs.getAttrs(), prov);
        mDn = dn;
    }

    public String getDN() {
        return mDn; 
    }
}
