// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.Filter;

import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;

public class UBIDLdapFilter extends ZLdapFilter {

    private Filter filter;

    UBIDLdapFilter(FilterId filterId, Filter filter) {
        super(filterId);
        this.filter = filter;
    }

    @Override
    public void debug() {
    }

    Filter getNative() {
        return filter;
    }

    @Override
    public String toFilterString() {
        // cannot use this one, assertion values are all turned to lower case
        // return getNative().toNormalizedString();
        return getNative().toString();
    }
}
