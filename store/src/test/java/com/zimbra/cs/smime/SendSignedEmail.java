package com.zimbra.cs.smime;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SendSignedEmail {

    public static final String PASSWORD = "password";

    public static void main(String[] args) throws Exception {
// Step 1: Set up mail session
        Security.addProvider(new BouncyCastleProvider());
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.host");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("user@demo.zextras.io", "password");
            }
        });

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

        props.put("mail.smtp.ssl.socketFactory", sslSocketFactory);
        // Step 2: Load private key and certificate (from a .p12 file)
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream("path/to/keystore.p12");
        keystore.load(fis, PASSWORD.toCharArray());

        String alias = "user@demo.zextras.io";
        keystore.aliases().asIterator().forEachRemaining(System.out::println);
        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, PASSWORD.toCharArray());
        X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
        List<Certificate> certList = new ArrayList<>();
        certList.add(certificate);

        Certificate[] certificateChain = keystore.getCertificateChain(alias);
        certList.addAll(Arrays.asList(certificateChain).subList(1, certificateChain.length));

        JcaCertStore certs = new JcaCertStore(certList);

        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector caps = new SMIMECapabilityVector();

        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        caps.addCapability(SMIMECapability.dES_CBC);

        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

        IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
                new X500Name(certificate.getIssuerX500Principal().getName()), certificate.getSerialNumber());
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));
        SMIMESignedGenerator gen = new SMIMESignedGenerator();
        gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                .setProvider("BC")
                .setSignedAttributeGenerator(new AttributeTable(signedAttrs))
                .build("SHA1withRSA", privateKey, certificate));
        gen.addCertificates(certs);
        // Step 7: Create multipart for the signed message
        MimeBodyPart    msg = new MimeBodyPart();

        msg.setText("Hello world!");

        MimeMultipart mm = gen.generate(msg);
        MimeMessage body = new MimeMessage(session);
        Address fromUser = new InternetAddress("user@demo.zextras.io");
        Address toUser = new InternetAddress("user@demo.zextras.io");
        body.setFrom(fromUser);
        body.setRecipient(Message.RecipientType.TO, toUser);
        body.setSubject("example signed message");
        body.setContent(mm, mm.getContentType());
        body.saveChanges();
        Transport.send(body);
        System.out.println("Email sent successfully with S/MIME2 signature.");
    }
}
