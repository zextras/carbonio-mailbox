// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.zimbra.cs.ldap.LdapTODO.*;

/*
 * migration path for javax.naming.NamingEnumeration interface
 * 
 * TODO: delete this eventually and do everything the pure unboundid way
 * 
 * try to gather all searchDir calls to LdapHelper.searchDir
 */
@TODO
public interface ZSearchResultEnumeration {
    ZSearchResultEntry next() throws LdapException;
    boolean hasMore() throws LdapException;
    void close() throws LdapException;
}

