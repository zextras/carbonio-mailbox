// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

public interface GalSearchQueryCallback {

    /**
     * 
     * @return extra query to be ANDed with the query for GAL sync account search
     */
    String getMailboxSearchQuery();
    
    /**
     * 
     * @return extra query to be ANDed with the query for Zimbra GAL LDAP search
     */
    String getZimbraLdapSearchQuery();
}
