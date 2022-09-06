// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.SearchResultEntry;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.ZSearchResultEntry;

/** Represents one LDAP entry in a search result. */
public class UBIDSearchResultEntry extends ZSearchResultEntry {

  private SearchResultEntry searchResultEntry;
  private UBIDAttributes zAttributes;

  UBIDSearchResultEntry(SearchResultEntry searchResultEntry) {
    this.searchResultEntry = searchResultEntry;
    this.zAttributes = new UBIDAttributes(searchResultEntry);
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
