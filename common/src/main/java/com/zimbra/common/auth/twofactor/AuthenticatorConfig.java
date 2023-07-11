// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.auth.twofactor;


public class AuthenticatorConfig {
    private long secondsInTimeWindow;
    private int allowedOffset;

    public AuthenticatorConfig() {}

    public AuthenticatorConfig allowedWindowOffset(int offset) {
        this.allowedOffset = offset;
        return this;
    }

    public AuthenticatorConfig setWindowSize(long secondsInTimeWindow) {
        this.secondsInTimeWindow = secondsInTimeWindow;
        return this;
    }

    public long getWindowSize() {
        return secondsInTimeWindow;
    }

    public int getWindowRange() {
        return allowedOffset;
    }
}
