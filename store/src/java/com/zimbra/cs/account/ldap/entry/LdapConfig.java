// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;

/**
 * 
 * @author pshao
 *
 */
public class LdapConfig extends Config implements LdapEntry {
    
    private String mDn;
    
    public LdapConfig(String dn, ZAttributes attrs, Provisioning provisioning) throws LdapException {
        super(attrs.getAttrs(), provisioning);
        mDn = dn;
    }
    
    public String getDN() {
        return mDn;
    }
}
