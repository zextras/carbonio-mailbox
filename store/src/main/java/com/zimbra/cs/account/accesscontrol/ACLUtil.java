// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public final class ACLUtil {
    private static final String ACL_CACHE_KEY = "ENTRY.ACL_CACHE";

    private ACLUtil() {
    }

    /**
     * Returns all ACEs granted on the entry.
     *
     * @param entry the entry on which rights are granted
     * @return all ACEs granted on the entry.
     */
    public static List<ZimbraACE> getAllACEs(Entry entry) throws ServiceException {
        ZimbraACL acl = getACL(entry);
        return acl != null ? acl.getAllACEs() : null;
    }

    public static Set<ZimbraACE> getAllowedNotDelegableACEs(Entry entry)
    throws ServiceException {
        ZimbraACL acl = getACL(entry);
        return acl != null ? acl.getAllowedNotDelegableACEs() : null;
    }

    public static Set<ZimbraACE> getAllowedDelegableACEs(Entry entry)
    throws ServiceException {
        ZimbraACL acl = getACL(entry);
        return acl != null ? acl.getAllowedDelegableACEs() : null;
    }

    public static Set<ZimbraACE> getDeniedACEs(Entry entry) throws
    ServiceException {
        ZimbraACL acl = getACL(entry);
        return acl != null ? acl.getDeniedACEs() : null;
    }

    /**
     * Returns a Set of ACEs with the specified rights granted on the entry.
     *
     * @param entry the entry on which rights are granted
     * @param rights rights of interest
     * @return a Set of ACEs with the specified rights granted on the entry.
     */
    public static List<ZimbraACE> getACEs(Entry entry, Set<? extends Right> rights)
    throws ServiceException {
        ZimbraACL acl = getACL(entry);
        return acl != null ? acl.getACEs(rights) : null;
    }


    /**
     * Grant rights on a target entry.
     */
    public static List<ZimbraACE> grantRight(Provisioning prov, Entry target, Set<ZimbraACE> aces)
    throws ServiceException {
        for (ZimbraACE ace : aces) {
            ZimbraACE.validate(ace);
        }
        ZimbraACL acl = getACL(target, Boolean.TRUE);
        List<ZimbraACE> granted = null;

        if (acl == null) {
            acl = new ZimbraACL(aces);
            granted = acl.getAllACEs();
        } else {
            // Make a copy so we don't interfere with others that are using the acl.
            // This instance of acl will never be used in any AccessManager code path.
            // It only lives within this method for serialization.
            // serialize will erase the cached ZimbraACL object on the target object.
            // The new ACL will be loaded when it is needed.
            acl = acl.clone();
            granted = acl.grantAccess(aces);
        }

        serialize(prov, target, acl);

        PermissionCache.invalidateCache(target);

        return granted;
    }

    /**
     * Revoke(remove) rights from a target entry.
     * If a right was not previously granted on the target, NO error is thrown.
     * @return a Set of grants that are actually revoked by this call
     */
    public static List<ZimbraACE> revokeRight(Provisioning prov, Entry target, Set<ZimbraACE> aces)
    throws ServiceException {
        ZimbraACL acl = getACL(target, Boolean.TRUE);
        if (acl == null) {
            return new ArrayList<>(); // return empty list
        }
        // Make a copy so we don't interfere with others that are using the acl.
        // This instance of acl will never be used in any AccessManager code path.
        // It only lives within this method for serialization.
        // serialize will erase the cached ZimbraACL object on the target object.
        // The new ACL will be loaded when it is needed.
        acl = acl.clone();
        List<ZimbraACE> revoked = acl.revokeAccess(aces);
        serialize(prov, target, acl);

        PermissionCache.invalidateCache(target);

        return revoked;
    }

    /**
     * Persists grants in LDAP
     */
    private static void serialize(Provisioning prov, Entry entry, ZimbraACL acl)
    throws ServiceException {
        // modifyAttrs will erase cached ACL and permission cache on the target
        prov.modifyAttrs(entry, Collections.singletonMap(Provisioning.A_zimbraACE, acl.serialize()));
    }

    /**
     * Get cached grants, if not in cache, load from LDAP.
     *
     * @param entry
     * @return
     * @throws ServiceException
     */
    static ZimbraACL getACL(Entry entry) throws ServiceException {
        return getACL(entry, Boolean.FALSE);
    }
    /**
     * Get cached grants, if not in cache, load from LDAP.
     *
     * @param entry the LDAP entry object for which we need ACLs
     * @param loadFromLdap when true we always load from LDAP, when false we try the cache first.
     * @return
     * @throws ServiceException
     */
    static ZimbraACL getACL(Entry entry, boolean loadFromLdap) throws ServiceException {
        ZimbraACL acl = null;
        if (!loadFromLdap) {
            acl = (ZimbraACL) entry.getCachedData(ACL_CACHE_KEY);
        }
        if (acl != null) {
            return acl;
        } else {
            acl = null;
            String[] aces = entry.getMultiAttr(Provisioning.A_zimbraACE);
            if (aces.length == 0) {
                return null;
            } else {
                acl = new ZimbraACL(aces, TargetTypeLookup.getTargetType(entry), entry.getLabel());
                entry.setCachedData(ACL_CACHE_KEY, acl);
            }
        }
        return acl;
    }

}
