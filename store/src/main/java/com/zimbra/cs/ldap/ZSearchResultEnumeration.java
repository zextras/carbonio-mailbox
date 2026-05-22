// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import java.util.Iterator;

public class ZSearchResultEnumeration {

    private SearchResult searchResult;
    private Iterator<SearchResultEntry> entriesIter;

    public ZSearchResultEnumeration(SearchResult searchResult) {
        this.searchResult = searchResult;
        this.entriesIter = searchResult.getSearchEntries().iterator();
    }

    public void close() throws LdapException {
        // DO nothing
    }

    public boolean hasMore() throws LdapException {
        return entriesIter.hasNext();
    }

    public ZSearchResultEntry next() throws LdapException {
        SearchResultEntry searchResultEntry = entriesIter.next();
        return new ZSearchResultEntry(searchResultEntry);
    }
}
