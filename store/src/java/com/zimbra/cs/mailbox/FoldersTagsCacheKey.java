// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.util.memcached.MemcachedKey;
import com.zimbra.cs.memcached.MemcachedKeyPrefix;

public class FoldersTagsCacheKey implements MemcachedKey {
    private String mKeyStr;

    public FoldersTagsCacheKey(String accountId) {
        mKeyStr = accountId;
    }

    public boolean equals(Object other) {
        if (other instanceof FoldersTagsCacheKey) {
            FoldersTagsCacheKey otherKey = (FoldersTagsCacheKey) other;
            return mKeyStr.equals(otherKey.mKeyStr);
        }
        return false;
    }

    public int hashCode() {
        return mKeyStr.hashCode();
    }

    // MemcachedKey interface
    public String getKeyPrefix() { return MemcachedKeyPrefix.MBOX_FOLDERS_TAGS; }
    public String getKeyValue() { return mKeyStr; }
}
