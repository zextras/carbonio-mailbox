/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.nginx;

public abstract class LookupEntry {
    private String mKey;
    
    LookupEntry(String key) {
        mKey = key;
    }
    
    String getKey() {
        return mKey;
    }
}
