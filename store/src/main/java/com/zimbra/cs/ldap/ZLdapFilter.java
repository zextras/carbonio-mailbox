// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;

public abstract class ZLdapFilter extends ZLdapElement {

  private FilterId filterId;

  protected ZLdapFilter(FilterId filterId) {
    this.filterId = filterId;
  }

  public abstract String toFilterString();

  public FilterId getFilterId() {
    return filterId;
  }

  public String getStatString() {
    return filterId.getStatString();
  }
}
