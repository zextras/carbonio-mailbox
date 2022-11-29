// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util.ngxlookup;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.zimbra.common.util.ZimbraLog;

public class NginxAuthServer {
    private String nginxAuthServer;
    private String nginxAuthUser;

    public NginxAuthServer(String authServer, String authPort, String authUser) {
        String format = "%s:%s";
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(authServer);
            if (inetAddress instanceof Inet6Address) {
                format = "[%s]:%s";
            }
        } catch (UnknownHostException e) {
            ZimbraLog.misc.error("Failed to recognize address %s", authServer, e);
        }
        this.nginxAuthServer = String.format(format, authServer, authPort);
        this.nginxAuthUser = authUser;
    }

    public String getNginxAuthServer() { return nginxAuthServer; }
    public String getNginxAuthUser() { return nginxAuthUser; }
}
