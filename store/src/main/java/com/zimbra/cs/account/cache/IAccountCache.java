// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import com.zimbra.cs.account.Account;

public interface IAccountCache extends IEntryCache {
  public void clear();

  public void remove(Account entry);

  public void put(Account entry);

  public void replace(Account entry);

  public Account getById(String key);

  public Account getByName(String key);

  public Account getByForeignPrincipal(String key);
}
