// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import com.zimbra.cs.account.Account;

public interface IAccountCache extends IEntryCache {
    void clear();
    void remove(Account entry);
    void put(Account entry);
    void replace(Account entry);
    Account getById(String key);
    Account getByName(String key);
    Account getByForeignPrincipal(String key);
}
