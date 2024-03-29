// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapZimlet extends Zimlet implements LdapEntry {

    private String mDn;
    
	public LdapZimlet(String dn, ZAttributes attrs, Provisioning prov) throws LdapException {
        super(attrs.getAttrString(Provisioning.A_cn), 
                attrs.getAttrString(Provisioning.A_cn),                 
                attrs.getAttrs(), prov);
        mDn = dn;
	}
	
    public String getDN() {
        return mDn;
    }
}
