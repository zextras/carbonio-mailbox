// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import com.zimbra.cs.account.NamedEntry;
import java.util.List;

public interface INamedEntryCache<E extends NamedEntry> extends IEntryCache {
  public void clear();

  public void remove(String name, String id);

  public void remove(E entry);

  public void put(E entry);

  public void replace(E entry);

  public void put(List<E> entries, boolean clear);

  public E getById(String key);

  public E getByName(String key);
}
