// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

import com.zimbra.cs.account.ldap.entry.LdapEntry;

/**
 * @author zimbra
 *
 */
public class AddressList extends NamedEntry implements LdapEntry {

    private String mDn;

    /**
     * @param dn
     * @param name
     * @param id
     * @param attrs
     * @param defaults
     * @param prov
     */
    public AddressList(String dn, String name, String id, Map<String, Object> attrs,
        Map<String, Object> defaults, Provisioning prov) {
        this(name, id, attrs, defaults, prov);
        this.mDn = dn;
    }

    /**
     * @param name
     * @param id
     * @param attrs
     * @param defaults
     * @param prov
     */
    public AddressList(String name, String id, Map<String, Object> attrs,
                          Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, defaults, prov);
        
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.ADDRESS_LIST;
    }
    
    public boolean isActive() {
        return getBooleanAttr(Provisioning.A_zimbraIsAddressListActive, false);
    }

    public String getGalSearchQuery() {
        return getAttr(Provisioning.A_zimbraAddressListGalFilter);
    }
    public String getLdapSearchQuery() {
        return getAttr(Provisioning.A_zimbraAddressListLdapFilter);
    }
    
    public String getDisplayName() {
        return getAttr(Provisioning.A_displayName);
    }

    public String getDN() {
        return mDn;
    }
}
