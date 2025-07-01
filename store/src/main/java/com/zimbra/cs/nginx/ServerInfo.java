/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.nginx;

public class ServerInfo extends LookupEntry {
    private static final String ZX_HTTP_PORT = "8742";
    private static final String ZX_HTTPS_PORT = "8743";

    private String mHttpPort;
    private String mHttpSSLPort;
    private String mHttpAdminPort;
    private String mHttpPop3Port;
    private String mHttpPop3SSLPort;
    private String mHttpImapPort;
    private String mHttpImapSSLPort;
    
    ServerInfo(String serverName) {
        super(serverName);
    }
    
    void setHttpPort(String port) {
        mHttpPort = port;
    }
    
    void setHttpSSLPort(String port) {
        mHttpSSLPort = port;
    }
    
    void setHttpAdminPort(String port) {
        mHttpAdminPort = port;
    }
    
    void setPop3Port(String port) {
        mHttpPop3Port = port;
    }
    
    void setPop3SSLPort(String port) {
        mHttpPop3SSLPort = port;
    }
    
    void setImapPort(String port) {
        mHttpImapPort = port;
    }
    
    void setImapSSLPort(String port) {
        mHttpImapSSLPort = port;
    }
    
    String getPortForProto(String proto, NginxLookupExtension.NginxLookupRequestType type) {
        if (NginxLookupExtension.NginxLookupHandler.POP3.equalsIgnoreCase(proto))
            return mHttpPop3Port;
        else if (NginxLookupExtension.NginxLookupHandler.POP3_SSL.equalsIgnoreCase(proto))
            return mHttpPop3SSLPort;
        else if (NginxLookupExtension.NginxLookupHandler.IMAP.equalsIgnoreCase(proto))
            return mHttpImapPort;
        else if (NginxLookupExtension.NginxLookupHandler.IMAP_SSL.equalsIgnoreCase(proto))
            return mHttpImapSSLPort;
        else if (NginxLookupExtension.NginxLookupHandler.HTTP.equalsIgnoreCase(proto)) {
            if (type == NginxLookupExtension.NginxLookupRequestType.zx) {
                return ZX_HTTP_PORT;
            } else {
                return mHttpPort;
            }
        } else if (NginxLookupExtension.NginxLookupHandler.HTTP_SSL.equalsIgnoreCase(proto)) {
            if (type == NginxLookupExtension.NginxLookupRequestType.zx) {
                return ZX_HTTPS_PORT;
            } else if (type == NginxLookupExtension.NginxLookupRequestType.admin) {
                return mHttpAdminPort;
            } else {
                return mHttpSSLPort;
            }
        }
        
        return null;
    }
}

