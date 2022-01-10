// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import java.util.Map;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapServer extends Server implements LdapEntry {

    private String mDn;

    public LdapServer(String dn, ZAttributes attrs, Map<String,Object> defaults, Provisioning prov) throws LdapException {
        super(attrs.getAttrString(Provisioning.A_cn), 
                attrs.getAttrString(Provisioning.A_zimbraId), 
                attrs.getAttrs(), defaults, prov);
        mDn = dn;
    }

    public String getDN() {
        return mDn;
    }
}
