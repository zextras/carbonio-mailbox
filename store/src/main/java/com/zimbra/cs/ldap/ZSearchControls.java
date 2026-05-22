// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import java.util.Arrays;
import java.util.List;

public class ZSearchControls {

    public static final int SIZE_UNLIMITED  = 0;
    public static final int TIME_UNLIMITED  = 0;
    public static final String[] RETURN_ALL_ATTRS = null;

    private final ZSearchScope searchScope;
    private final int sizeLimit;
    private final List<String> returnAttrs;

    private ZSearchControls(ZSearchScope searchScope, int sizeLimit, String[] returnAttrs) {
        this.searchScope = searchScope;
        this.sizeLimit = sizeLimit;
        this.returnAttrs = returnAttrs != null ? Arrays.asList(returnAttrs) : null;
    }

    public static ZSearchControls SEARCH_CTLS_SUBTREE() {
        return createSearchControls(ZSearchScope.SEARCH_SCOPE_SUBTREE,
                SIZE_UNLIMITED, RETURN_ALL_ATTRS);
    }

    public static ZSearchControls createSearchControls(ZSearchScope searchScope,
            int sizeLimit, String[] returnAttrs) {
        return new ZSearchControls(searchScope, sizeLimit, returnAttrs);
    }

    public ZSearchScope getSearchScope() {
        return searchScope;
    }

    public int getSizeLimit() {
        return sizeLimit;
    }

    public int getTimeLimit() {
        return TIME_UNLIMITED;
    }

    public boolean getTypesOnly() {
        return false;
    }

    public List<String> getReturnAttrs() {
        return returnAttrs;
    }
}
