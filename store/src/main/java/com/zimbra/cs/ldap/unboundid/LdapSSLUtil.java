// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.unboundid.util.ssl.SSLUtil;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.net.TrustManagers;
import com.zimbra.cs.ldap.LdapException;

public class LdapSSLUtil {
    
    /*
    private static final LdapSSLUtil SINGLETON = new LdapSSLUtil();
    
    private static SSLUtil sslUtil;
    
    static LdapSSLUtil getInstance() {
        return SINGLETON;
    }
    
    private LdapSSLUtil() {
        boolean allowUntrustedCerts = LC.ssl_allow_untrusted_certs.booleanValue();
        TrustManager tm = getTrustManager(allowUntrustedCerts);
        sslUtil = new SSLUtil(tm);
    }
    
    private TrustManager getTrustManager(boolean allowUntrustedCerts) {
        if (allowUntrustedCerts) {
            return TrustManagers.dummyTrustManager();
        } else {
            return TrustManagers.customTrustManager();
        }
    }
    
    synchronized SSLSocketFactory createSocketFactory() throws LdapException {
        try {
            return sslUtil.createSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            throw LdapException.LDAP_ERROR(e);
        }
    }
    
    synchronized SSLContext createSSLContext() throws LdapException  {
        try {
            return sslUtil.createSSLContext();
        } catch (GeneralSecurityException e) {
            throw LdapException.LDAP_ERROR(e);
        }
    }
    */
    
    static SSLSocketFactory createSSLSocketFactory(boolean allowUntrustedCerts) {
        if (allowUntrustedCerts) {
            return SocketFactories.dummySSLSocketFactory();
        } else {
            return SocketFactories.defaultSSLSocketFactory();
        }
    }
    
    private static TrustManager getTrustManager(boolean allowUntrustedCerts) {
        if (allowUntrustedCerts) {
            return TrustManagers.dummyTrustManager();
        } else {
            return TrustManagers.customTrustManager();
        }
    }
    
    static SSLContext createSSLContext(boolean allowUntrustedCerts) throws LdapException {
        TrustManager tm = getTrustManager(allowUntrustedCerts);
        SSLUtil sslUtil = new SSLUtil(tm);
        
        try {
            return sslUtil.createSSLContext();
        } catch (GeneralSecurityException e) {
            throw UBIDLdapException.mapToLdapException(e);
        }
    }
}
