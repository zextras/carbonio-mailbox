// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

public class JWTInfo {

    private String salt;
    private long expiryTime;

    public JWTInfo(String salt, long expiryTime) {
        this.salt = salt;
        this.expiryTime = expiryTime;
    }

    public String getSalt() {
        return salt;
    }
    public long getExpiryTime() {
        return expiryTime;
    }
}
