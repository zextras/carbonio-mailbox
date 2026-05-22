// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.Filter;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;

public class ZLdapFilter extends ZLdapElement {

    private FilterId filterId;
    private Filter filter;

    public ZLdapFilter(FilterId filterId, Filter filter) {
        this.filterId = filterId;
        this.filter = filter;
    }

    @Override
    public void debug() {
    }

    public Filter getNative() {
        return filter;
    }

    public FilterId getFilterId() {
        return filterId;
    }

    public String getStatString() {
        return filterId.getStatString();
    }

    public String toFilterString() {
        // cannot use this one, assertion values are all turned to lower case
        // return getNative().toNormalizedString();
        return getNative().toString();
    }
}
