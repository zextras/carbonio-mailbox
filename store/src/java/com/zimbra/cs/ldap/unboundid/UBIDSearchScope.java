// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.SearchScope;

import com.zimbra.cs.ldap.ZSearchScope;

public class UBIDSearchScope extends ZSearchScope {

    final private SearchScope searchScope;
    
    private UBIDSearchScope(SearchScope searchScope) {
        this.searchScope = searchScope;
    }
    
    SearchScope getNative() {
        return searchScope;
    }

    public static class UBIDSearchScopeFactory extends ZSearchScope.ZSearchScopeFactory {
        @Override
        protected ZSearchScope getBaseSearchScope() {
            return new UBIDSearchScope(SearchScope.BASE);
        }
        
        @Override
        protected ZSearchScope getOnelevelSearchScope() {
            return new UBIDSearchScope(SearchScope.ONE);
        }
        
        @Override
        protected ZSearchScope getSubtreeSearchScope() {
            return new UBIDSearchScope(SearchScope.SUB);
        }

        @Override
        protected ZSearchScope getChildrenSearchScope() {
            return new UBIDSearchScope(SearchScope.SUBORDINATE_SUBTREE);
        }
    }

}
