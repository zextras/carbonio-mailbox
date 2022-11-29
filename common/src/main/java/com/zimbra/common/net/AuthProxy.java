// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.net.Proxy;
import java.net.SocketAddress;
/**
 * Proxy that includes username password authentication details
 */
public class AuthProxy extends Proxy {

    private String username;
    private String password;
    private Type type;
    
    public AuthProxy(Type type, SocketAddress sa) {
        super(type, sa);
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Type getType() {
        return type;
    }
}
