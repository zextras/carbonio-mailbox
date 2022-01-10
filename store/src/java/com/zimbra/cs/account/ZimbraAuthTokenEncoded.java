// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

public class ZimbraAuthTokenEncoded extends ZimbraAuthToken {
    private String encoded;
    
    public ZimbraAuthTokenEncoded(String encoded) {
        this.encoded = encoded;
    }
    
    @Override
    public String getEncoded() {
        return encoded;
    }
}
