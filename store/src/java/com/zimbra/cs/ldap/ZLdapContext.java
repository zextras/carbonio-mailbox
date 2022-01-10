// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.LdapException.LdapEntryAlreadyExistException;

public abstract class ZLdapContext extends ZLdapElement implements ILdapContext {
    protected LdapUsage usage;

    protected ZLdapContext(LdapUsage usage) {
        this.usage = usage;
    }

    public LdapUsage getUsage() {
        return usage;
    }

    public abstract void closeContext(boolean forceCloseConnection);

    public abstract void createEntry(ZMutableEntry entry) throws
    LdapEntryAlreadyExistException, ServiceException;

    public abstract void createEntry(String dn, String objectClass, String[] attrs)
    throws ServiceException;

    public abstract void createEntry(String dn, String[] objectClasses, String[] attrs)
    throws ServiceException;

    public abstract ZModificationList createModificationList();

    public abstract void deleteChildren(String dn) throws ServiceException;

    public abstract ZAttributes getAttributes(String dn, String[] attrs) throws LdapException;

    public abstract ZLdapSchema getSchema() throws LdapException;

    public abstract void modifyAttributes(String dn, ZModificationList modList)
    throws LdapException;

    public abstract boolean testAndModifyAttributes(String dn, ZModificationList modList,
            ZLdapFilter testFilter) throws LdapException;

    public abstract void moveChildren(String oldDn, String newDn) throws ServiceException;

    public abstract void renameEntry(String oldDn, String newDn) throws LdapException;

    public abstract void replaceAttributes(String dn, ZAttributes attrs)
    throws LdapException;

    /**
     * This API does paged search, results are returned via the search
     * visitor interface.
     *
     * If a size limit is set (on the SearchLdapOptions or on the
     * LDAP server), LdapSizeLimitExceededException will be thrown.
     * Partial results are still returned to the visitor.
     */
    public abstract void searchPaged(SearchLdapOptions searchOptions)
    throws ServiceException;

    /**
     * This API is for searches that should not hit too many entries.
     * It does not use the LDAP paging control.
     *
     * Results are returned in a ZSearchResultEnumeration, which should
     * accessed in the pattern:
     *     ZSearchResultEnumeration sr = zlc.searchDir(dn, filter, searchControls);
     *         while (sr.hasMore()) {
     *             ZSearchResultEntry entry = sr.next();
     *             // use the entry
     *         }
     *     sr.close();
     *
     *
     * If a size limit is set (on the ZSearchControls or on the
     * LDAP server), LdapSizeLimitExceededException will be thrown.
     *
     * IMPORTANT notes on LdapSizeLimitExceededException
     *   - the legacy JNDI implementation still returns the
     *     ZSearchResultEnumeration.  LdapSizeLimitExceededException
     *     is thrown when the ZSearchResultEnumeration.hasMore()
     *     is called.
     *
     *   - the unboundid implementation throws LdapSizeLimitExceededException
     *     on the searchDir() call.
     *
     *   The legacy behavior should NOT be relied on.
     */
    public abstract ZSearchResultEnumeration searchDir(
            String baseDN, ZLdapFilter filter, ZSearchControls searchControls)
    throws LdapException;

    /** TODO: Make abstract for Kiss and later versions */
    public boolean compare(final String dn, final String attributeName, final String assertionValue)
    throws ServiceException {
        throw new UnsupportedOperationException("compare operation has not been implemented");
    }

    public abstract long countEntries(
            String baseDN, ZLdapFilter filter, ZSearchControls searchControls)
    throws LdapException;

    public abstract void deleteEntry(String dn) throws LdapException;

    public abstract void setPassword(String dn, String newPassword) throws LdapException;

}
