// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import java.util.Map;

import com.zimbra.cs.account.AlwaysOnCluster;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;


public class LdapAlwaysOnCluster extends AlwaysOnCluster implements LdapEntry {

    private final String mDn;

    public LdapAlwaysOnCluster(String dn, ZAttributes attrs, Map<String,Object> defaults, Provisioning prov) throws LdapException {
        super(attrs.getAttrString(Provisioning.A_cn),
                attrs.getAttrString(Provisioning.A_zimbraId),
                attrs.getAttrs(), defaults, prov);
        mDn = dn;
    }

    @Override
    public String getDN() {
        return mDn;
    }
}
