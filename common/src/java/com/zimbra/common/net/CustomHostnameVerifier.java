// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.bouncycastle.est.jcajce.JsseDefaultHostnameAuthorizer;

import com.google.common.collect.Sets;
import com.zimbra.common.util.ZimbraLog;


public class CustomHostnameVerifier implements HostnameVerifier {


    public static void verifyHostname(String hostname, SSLSession session) throws IOException {
        if (NetConfig.getInstance().isAllowMismatchedCerts()) return;

        try {
            InetAddress.getByName(hostname);
        } catch (UnknownHostException uhe) {
            throw new UnknownHostException("Could not resolve SSL sessions server hostname: " + hostname);
        }

        javax.security.cert.X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs == null || certs.length == 0)
            throw new SSLPeerUnverifiedException("No server certificates found: " + hostname);

        X509Certificate cert = certJavax2Java(certs[0]);

        CustomTrustManager ctm = TrustManagers.customTrustManager();
        if (ctm.isCertificateAcceptedForHostname(hostname, cert))
            return;
        Set<String> knownSuffixes = Sets.newHashSet();
        JsseDefaultHostnameAuthorizer hc = new JsseDefaultHostnameAuthorizer(knownSuffixes);
        hc.verify(hostname, cert);

    }

    private static java.security.cert.X509Certificate certJavax2Java(javax.security.cert.X509Certificate cert) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(cert.getEncoded());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (java.security.cert.X509Certificate) cf.generateCertificate(bis);
        } catch (java.security.cert.CertificateEncodingException e) {
        } catch (javax.security.cert.CertificateEncodingException e) {
        } catch (java.security.cert.CertificateException e) {
        }
        return null;
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        try {
            verifyHostname(hostname, session);
        } catch (IOException e) {
            ZimbraLog.security.debug(
                "Hostname verification failed: hostname = " + hostname, e);
            return false;
        }
        return true;
    }
}
