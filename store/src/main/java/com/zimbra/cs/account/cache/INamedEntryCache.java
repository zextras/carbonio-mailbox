// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import java.util.List;

import com.zimbra.cs.account.NamedEntry;

public interface INamedEntryCache<E extends NamedEntry> extends IEntryCache {
    void clear();
    void remove(String name, String id);
    void remove(E entry);
    void put(E entry);
    void replace(E entry);
    void put(List<E> entries, boolean clear);
    E getById(String key);
    E getByName(String key);
}
