// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZSearchResultEntry;

/**
 * 
 * @author pshao
 *
 */
public class LdapMimeType extends LdapMimeTypeBase {
       
    public LdapMimeType(ZSearchResultEntry entry, Provisioning prov) throws LdapException {
        super(entry.getAttributes().getAttrs(), null, prov);
        mDn = entry.getDN();
    }

}
