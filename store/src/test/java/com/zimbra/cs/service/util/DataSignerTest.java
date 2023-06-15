// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.Test;
import com.google.common.io.ByteStreams;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class DataSignerTest {

 @Test
 void testSignData() {
  try {
   String serverdir = MailboxTestUtil.getZimbraServerDir("");
   FileInputStream p12Stream = new FileInputStream(serverdir + "data/unittest/certificate/sign1_digitalid.p12");
   char[] expPass = "test123export".toCharArray();
   byte[] certBytes = ByteStreams.toByteArray(p12Stream);
   byte[] signedData = DataSigner.signData("hello world".getBytes(), certBytes, expPass);
   // validate signed data
   ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
   try (ASN1InputStream asnInputStream = new ASN1InputStream(inputStream)) {
    CMSSignedData cmsSignedData = new CMSSignedData(ContentInfo.getInstance(asnInputStream.readObject()));
    Store certs = cmsSignedData.getCertificates();
    SignerInformationStore signers = cmsSignedData.getSignerInfos();
    Collection<SignerInformation> c = signers.getSigners();
    Iterator<SignerInformation> it = c.iterator();
    SignerInformation signer = it.next();
    Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
    X509CertificateHolder certHolder = certCollection.iterator().next();
    boolean verify = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder));
    assertTrue(verify);
   }
  } catch (Exception e) {
   e.printStackTrace();
   fail("data sign test failed");
  }
 }
}
