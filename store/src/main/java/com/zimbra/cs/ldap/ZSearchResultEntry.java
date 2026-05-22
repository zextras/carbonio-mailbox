// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.SearchResultEntry;

/**
 * Represents one LDAP entry in a search result.
 */
public class ZSearchResultEntry extends ZEntry {

    private SearchResultEntry searchResultEntry;
    private ZAttributes zAttributes;

    public ZSearchResultEntry(SearchResultEntry searchResultEntry) {
        this.searchResultEntry = searchResultEntry;
        this.zAttributes = new ZAttributes(searchResultEntry);
    }

    @Override
    public void debug() {
        println(searchResultEntry.toString());
    }

    @Override
    public ZAttributes getAttributes() {
        return zAttributes;
    }

    @Override
    public String getDN() {
        return searchResultEntry.getDN();
    }
}
