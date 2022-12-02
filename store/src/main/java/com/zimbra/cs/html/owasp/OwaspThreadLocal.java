// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

public class OwaspThreadLocal {

    private String baseHref;

    private String vHost;

    public String getBaseHref() {
        return baseHref;
    }

    public void setBaseHref(String baseHref) {
        this.baseHref = baseHref;
    }

    public String getVHost() {
        return vHost;
    }

    public void setVHost(String vHost) {
        this.vHost = vHost;
    }
}
