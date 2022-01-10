// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.common.util.memcached.MemcachedKey;
import com.zimbra.cs.memcached.MemcachedKeyPrefix;

// cache key for a calendar folder in an account
public class CalendarKey implements MemcachedKey {
    private String mAccountId;
    private int mFolderId;
    private String mKeyVal;

    public CalendarKey(String accountId, int folderId) {
        mAccountId = accountId;
        mFolderId = folderId;
        mKeyVal = mAccountId + ":" + folderId;
    }

    public String getAccountId() { return mAccountId; }
    public int getFolderId() { return mFolderId; }

    public boolean equals(Object other) {
        if (other instanceof CalendarKey) {
            CalendarKey otherKey = (CalendarKey) other;
            return mKeyVal.equals(otherKey.mKeyVal);
        }
        return false;
    }

    public int hashCode()    { return mKeyVal.hashCode(); }
    public String toString() { return mKeyVal; }

    // MemcachedKey interface
    public String getKeyPrefix() { return MemcachedKeyPrefix.CTAGINFO; }
    public String getKeyValue() { return mKeyVal; }
}
