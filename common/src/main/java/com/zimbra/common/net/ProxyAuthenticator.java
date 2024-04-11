// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.util.ZimbraLog;

/**
 * Authenticator implementation for HTTP/SOCKS proxy username password
 */
public class ProxyAuthenticator extends Authenticator {
    
    private Map<Proxy.Type, UsernamePassword> userPasswords = new HashMap<>();
    
    public ProxyAuthenticator() {
        super();
    }
    
    public void addCredentials(Proxy.Type type, UsernamePassword userPass) {
        userPasswords.put(type, userPass);
    }
    
    private UsernamePassword getUsernamePassword() {
        String reqProto = getRequestingProtocol().toLowerCase();
        UsernamePassword uPass = null;
        if (reqProto.startsWith("http")) {
            uPass = userPasswords.get(Proxy.Type.HTTP);
        } else if (reqProto.startsWith("sock")) {
            uPass = userPasswords.get(Proxy.Type.SOCKS);
        } 
        if (uPass == null) {
            throw new RuntimeException();
        } else {
            return uPass;
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType() == RequestorType.PROXY) {
            UsernamePassword uPass = getUsernamePassword();
            return new PasswordAuthentication(uPass.getUsername(), uPass.getPassword().toCharArray());
        } else {
            ZimbraLog.net.warn("Non-proxy authentication type %s requested, unable to fulfil", getRequestorType());
            return null;
        }
    }
    
}
