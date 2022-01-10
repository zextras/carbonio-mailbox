// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;

/**
 * @author pshao
 */
public abstract class DynamicGroup extends ZAttrDynamicGroup {

    private Boolean hasCustomMemberURL = null;

    public DynamicGroup(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.DYNAMICGROUP;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public Domain getDomain() throws ServiceException {
        return getProvisioning().getDomain(this);
    }

    @Override  // Override in LdapDynamicGroup and SoapDynamicGroup
    public String[] getAllMembers() throws ServiceException {
        return getMultiAttr(Provisioning.A_member);
    }

    @Override  // overriden on LdapDynamicGroup
    public Set<String> getAllMembersSet() throws ServiceException {
        return getMultiAttrSet(Provisioning.A_member);
    }

    @Override
    public String[] getAliases() throws ServiceException {
        return getMailAlias();
    }

    /*
     * Override in LdapDynamicGroup
     *
     * Default implementation is calling getAllMembers() regardless
     * of supportNonDefaultMemberURL.
     *
     * Should only be called from the edge: ProvUtil or adminNamespace
     * GetDistributuionList.  If supportNonDefaultMemberURL is true,
     * this call can be very expensive.
     */
    public String[] getAllMembers(boolean supportNonDefaultMemberURL)
    throws ServiceException {
        return getAllMembers();
    }

    public boolean isMembershipDefinedByCustomURL() {
        if (hasCustomMemberURL == null) {
            hasCustomMemberURL = isMembershipDefinedByCustomURL(getMemberURL());
        }
        return hasCustomMemberURL;
    }

    public static boolean isMembershipDefinedByCustomURL(String memberURL) {
        return ((memberURL != null) && (!memberURL.startsWith("ldap:///??sub?(|(zimbraMemberOf=")));
    }
}
