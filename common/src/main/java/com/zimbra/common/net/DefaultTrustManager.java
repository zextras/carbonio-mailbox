// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.zimbra.common.localconfig.LC;

/**
 * Default cacerts backed trust manager
 *
 * @author jjzhuang
 */
class DefaultTrustManager implements X509TrustManager {
    private X509TrustManager keyStoreTrustManager;

    protected DefaultTrustManager() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream kin = null;
        try {
            kin = new FileInputStream(LC.mailboxd_truststore.value());
            try {
                keyStore.load(kin, LC.mailboxd_truststore_password.value().toCharArray());
            } catch (IOException x) {
                throw new KeyStoreException(x);
            }
        } catch (FileNotFoundException x) {
            throw new KeyStoreException(x);
        } finally {
            if (kin != null)
                try {
                    kin.close();
                } catch (IOException x) {
                    throw new KeyStoreException(x);
                }
        }

        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        TrustManager[] trustManagers = factory.getTrustManagers();
        for (TrustManager tm : trustManagers)
            if (tm instanceof X509TrustManager) {
                keyStoreTrustManager = (X509TrustManager)tm;
                return;
            }
        throw new KeyStoreException(TrustManagerFactory.getDefaultAlgorithm() + " trust manager not supported");
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        keyStoreTrustManager.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        keyStoreTrustManager.checkServerTrusted(chain, authType);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return keyStoreTrustManager.getAcceptedIssuers();
    }
}





