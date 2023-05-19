// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.cache.DomainCache.GetFromDomainCacheOption;

public interface IDomainCache extends IEntryCache {
    void clear();
    void remove(Domain entry);
    void replace(Domain entry);
    void removeFromNegativeCache(DomainBy domainBy, String key);
    void put(DomainBy domainBy, String key, Domain entry);
    Domain getById(String key, GetFromDomainCacheOption option);
    Domain getByName(String key, GetFromDomainCacheOption option);
    Domain getByVirtualHostname(String key, GetFromDomainCacheOption option);
    Domain getByForeignName(String key, GetFromDomainCacheOption option);
    Domain getByKrb5Realm(String key, GetFromDomainCacheOption option);
}
