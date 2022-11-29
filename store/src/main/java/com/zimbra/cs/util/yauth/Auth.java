// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

public interface Auth {
    String getAppId();
    String getWSSID();
    String getCookie();
    boolean isExpired();
}
