// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.common.util.memcached.MemcachedKey;
import com.zimbra.cs.memcached.MemcachedKeyPrefix;

// cache key for an account
public class AccountKey implements MemcachedKey {
    private String mAccountId;

    public AccountKey(String accountId) {
        mAccountId = accountId;
    }

    public String getAccountId() { return mAccountId; }

    public boolean equals(Object other) {
        if (other instanceof AccountKey) {
            AccountKey otherKey = (AccountKey) other;
            return mAccountId.equals(otherKey.mAccountId);
        }
        return false;
    }

    public int hashCode() {
        return mAccountId.hashCode();
    }

    // MemcachedKey interface
    public String getKeyPrefix() { return MemcachedKeyPrefix.CALENDAR_LIST; }
    public String getKeyValue() { return mAccountId; }
}
