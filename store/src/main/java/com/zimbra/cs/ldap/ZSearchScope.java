// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.SearchScope;

public class ZSearchScope {

    // only the entry specified by the base DN should be considered.
    public static final ZSearchScope SEARCH_SCOPE_BASE = new ZSearchScope(SearchScope.BASE);

    // only entries that are immediate subordinates of the entry specified
    // by the base DN (but not the base entry itself) should be considered.
    public static final ZSearchScope SEARCH_SCOPE_ONELEVEL = new ZSearchScope(SearchScope.ONE);

    // the base entry itself and any subordinate entries (to any depth) should be considered.
    public static final ZSearchScope SEARCH_SCOPE_SUBTREE = new ZSearchScope(SearchScope.SUB);

    // any subordinate entries (to any depth) below the entry specified by the base DN should
    // be considered, but the base entry itself should not be considered.
    public static final ZSearchScope SEARCH_SCOPE_CHILDREN = new ZSearchScope(SearchScope.SUBORDINATE_SUBTREE);

    private final SearchScope searchScope;

    private ZSearchScope(SearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public SearchScope getNative() {
        return searchScope;
    }
}
