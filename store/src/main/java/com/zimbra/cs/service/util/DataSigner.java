// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

public class DataSigner {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] signData(byte[] data, byte[] digiCert, char[] expPass)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableKeyException, OperatorCreationException, CMSException {
        byte[] signedData = null;
        try (InputStream targetStream = new ByteArrayInputStream(digiCert)) {
            KeyStore pkcs12Store = KeyStore.getInstance("PKCS12");
            pkcs12Store.load(targetStream, expPass);
            String alias = null;
            for (Enumeration<String> en = pkcs12Store.aliases(); en.hasMoreElements();) {
                alias = en.nextElement();
            }
            X509Certificate cert = (X509Certificate) pkcs12Store.getCertificate(alias);
            PrivateKey privKey = (PrivateKey) pkcs12Store.getKey(alias, expPass);
            signedData = signData(data, cert, privKey);
        }
        return signedData;
    }

    public static byte[] signData(byte[] data, X509Certificate signingCertificate, PrivateKey signingKey)
            throws CertificateEncodingException, OperatorCreationException, CMSException, IOException {
        byte[] signedData = null;
        CMSTypedData cmsData = new CMSProcessableByteArray(data);
        List<X509Certificate> certList = new ArrayList<>();
        certList.add(signingCertificate);

        Store certs = new JcaCertStore(certList);
        CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);
        cmsGenerator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                        .build(contentSigner, signingCertificate));
        cmsGenerator.addCertificates(certs);
        CMSSignedData cms = cmsGenerator.generate(cmsData, true);
        signedData = cms.getEncoded();
        return signedData;
    }

}
