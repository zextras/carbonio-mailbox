// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

public abstract class ZSearchScope {
  // only the entry specified by the base DN should be considered.
  public static ZSearchScope SEARCH_SCOPE_BASE;

  // only entries that are immediate subordinates of the entry specified
  // by the base DN (but not the base entry itself) should be considered.
  public static ZSearchScope SEARCH_SCOPE_ONELEVEL;

  // the base entry itself and any subordinate entries (to any depth) should be considered.
  public static ZSearchScope SEARCH_SCOPE_SUBTREE;

  // any subordinate entries (to any depth) below the entry specified by the base DN should
  // be considered, but the base entry itself should not be considered.
  public static ZSearchScope SEARCH_SCOPE_CHILDREN;

  public abstract static class ZSearchScopeFactory {
    protected abstract ZSearchScope getBaseSearchScope();

    protected abstract ZSearchScope getOnelevelSearchScope();

    protected abstract ZSearchScope getSubtreeSearchScope();

    protected abstract ZSearchScope getChildrenSearchScope();
  }

  public static void init(ZSearchScopeFactory factory) {
    SEARCH_SCOPE_BASE = factory.getBaseSearchScope();
    SEARCH_SCOPE_ONELEVEL = factory.getOnelevelSearchScope();
    SEARCH_SCOPE_SUBTREE = factory.getSubtreeSearchScope();
    SEARCH_SCOPE_CHILDREN = factory.getChildrenSearchScope();
  }
}
