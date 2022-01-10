// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.custom;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.account.SearchDirectoryOptions.ObjectType;
import com.zimbra.cs.account.SearchDirectoryOptions.SortOpt;
import com.zimbra.cs.account.ldap.LdapProvisioning;

public class CustomLdapProvisioning extends LdapProvisioning {

    @Override
    protected void setDIT() {
        mDIT = new CustomLdapDIT(this);
    }

    public CustomLdapProvisioning() {
        super();
    }

    public CustomLdapProvisioning(CacheMode cacheMode) {
        super(cacheMode);
    }

    @Override
    public List<?> getAllDistributionLists(Domain domain) throws ServiceException {
        /* Don't specify domain in constructor - custom DIT doesn't necessarily store groups under the domain */
        SearchDirectoryOptions searchOpts = new SearchDirectoryOptions();
        searchOpts.setFilter(mDIT.filterDistributionListsByDomain(domain));
        searchOpts.setTypes(ObjectType.distributionlists);
        searchOpts.setSortOpt(SortOpt.SORT_ASCENDING);
        return searchDirectoryInternal(searchOpts);
    }

    /**
     * Note: Only returns distributionlists.  Dynamic groups are not supported with customDIT
     */
    @Override
    public List getAllGroups(Domain domain) throws ServiceException {
        /* Note: If going to support dynamicgroups in the future, when specifying both groups and
         *       DLs in searchDirectoryInternal, it add a sub-tree match filter for the domain, which
         *       won't work because groups/DLs aren't necessarily stored under the domain sub-tree.
         *       Suggest doing 2 searches and combining the results
         */
        return getAllDistributionLists(domain);
    }

    /**
     * Always returns an empty GroupMembership as Dynamic groups are not supported with customDIT
     */
    @Override
    @VisibleForTesting
    public GroupMembership getCustomDynamicGroupMembership(Account acct, boolean adminGroupsOnly)
            throws ServiceException {
        return new GroupMembership();
    }


}
