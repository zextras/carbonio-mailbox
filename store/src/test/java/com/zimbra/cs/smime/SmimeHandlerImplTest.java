package com.zimbra.cs.smime;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.localconfig.LocalConfig;
import com.zimbra.common.soap.Element;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.OperationContext;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmimeHandlerImplTest {

    public static final String ISSUER_DN = "CN=MyCA, O=MyOrganization, C=US";
    private static MimeMessage message;
    private static KeyPair keyPair;
    private static ContentSigner signerIssuer;
    private static X509Certificate certificate;
    private static X509Certificate issuerCert;
    private Server server;

    @BeforeAll
    static void setUpAll() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        keyPair = keyPairGenerator.generateKeyPair();

        KeyPairGenerator keyPairIssuerGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairIssuerGenerator.initialize(2048, new SecureRandom());
        KeyPair issuerKeyPair = keyPairIssuerGenerator.generateKeyPair();
        // Create self-signed issuer certificate
        X500Name issuerName = new X500Name(ISSUER_DN);
        BigInteger issuerSerial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
        Date notAfter = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 765);
        X509v3CertificateBuilder issuerCertBuilder = new JcaX509v3CertificateBuilder(
                issuerName, issuerSerial, notBefore, notAfter, issuerName, issuerKeyPair.getPublic());
        issuerCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        ContentSigner issuerSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuerKeyPair.getPrivate());
        X509CertificateHolder issuerCertHolder = issuerCertBuilder.build(issuerSigner);
        issuerCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(issuerCertHolder);

        signerIssuer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuerKeyPair.getPrivate());

    }

    private static void setX509Certificate(Date notBefore, Date notAfter) throws Exception {
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                new X500Name(ISSUER_DN), BigInteger.valueOf(System.currentTimeMillis()),
                notBefore, notAfter, new X500Name("CN=user@demo.zextras.io"), keyPair.getPublic());
        GeneralName email = new GeneralName(GeneralName.rfc822Name, "user@demo.zextras.io");
        GeneralNames subjectAltNames = new GeneralNames(email);
        certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);

        certificate =  new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(certBuilder.build(signerIssuer));
    }

    private static void createSignedMimeMessage() throws Exception {
        Properties props = new Properties();

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("user@demo.zextras.io", "password");
            }
        });

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

        props.put("mail.smtp.ssl.socketFactory", sslSocketFactory);
        // Step 2: Load private key and certificate (from a .p12 file)

        List<Certificate> certList = new ArrayList<>();
        certList.add(certificate);
        certList.add(issuerCert);
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
                .build("SHA1withRSA", keyPair.getPrivate(), certificate));
        gen.addCertificates(certs);

        MimeBodyPart msg = new MimeBodyPart();

        msg.setText("Hello world!");

        MimeMultipart mm = gen.generate(msg);
        message = new MimeMessage(session);
        Address fromUser = new InternetAddress("user@demo.zextras.io");
        Address toUser = new InternetAddress("user@demo.zextras.io");
        message.setFrom(fromUser);
        message.setRecipient(Message.RecipientType.TO, toUser);
        message.setSubject("example signed message");
        message.setContent(mm, mm.getContentType());
        message.saveChanges();
    }

    @BeforeEach
    void setUp() throws Exception {
        Field trustStore = SmimeHandlerImpl.class.getDeclaredField("trustStore");
        trustStore.setAccessible(true);
        trustStore.set(null, null);
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.remove(LC.mailboxd_truststore.key());
        localConfig.save();

        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Provisioning provisioning = Mockito.mock();
        Field singleton = Provisioning.class.getDeclaredField("singleton");
        singleton.setAccessible(true);
        singleton.set(null, provisioning);
        server = Mockito.mock();
        Mockito.when(provisioning.getLocalServer()).thenReturn(server);
        Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
        Date notAfter = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365);
        setX509Certificate(notBefore, notAfter);
        createSignedMimeMessage();
    }

    @AfterEach
    void tearDown() throws Exception {
        setUp();
        Field singleton = Provisioning.class.getDeclaredField("singleton");
        singleton.setAccessible(true);
        singleton.set(null, null);
    }

    @ParameterizedTest
    @MethodSource("provideDataExtractCN")
    void test_extractCN(String name, String output) {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        X500Principal principal = Mockito.mock();
        Mockito.when(principal.getName()).thenReturn(name);
        String result = smimeHandler.extractCN(principal);
        assertEquals(output, result);

    }

    static Stream<Arguments> provideDataExtractCN() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("1", ""),
                org.junit.jupiter.params.provider.Arguments.of("CN", ""),
                org.junit.jupiter.params.provider.Arguments.of("CN=", ""),
                org.junit.jupiter.params.provider.Arguments.of("cn=", ""),
                org.junit.jupiter.params.provider.Arguments.of("Cn=test", "test"),
                org.junit.jupiter.params.provider.Arguments.of("CN=test2", "test2"),
                org.junit.jupiter.params.provider.Arguments.of("cn=test3", "test3"),
                org.junit.jupiter.params.provider.Arguments.of("cn=test3,cn=test4", "test3"),
                org.junit.jupiter.params.provider.Arguments.of("abc=xyz,cn=test3,cn=test4", "test3")
        );


    }

    @Test
    void test_getKeyStore_when_store_is_not_valid_then_throws_FileNotFoundException() {
        Assertions.assertThrowsExactly(FileNotFoundException.class, SmimeHandlerImpl::getKeyStore);
    }

    @Test
    void test_getKeyStore_when_store_is_valid_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key(), javaHome + "/lib/security/cacerts");
        localConfig.save();
        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Assertions.assertNotNull(SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getKeyStore_when_store_is_valid_and_cache_is_not_invalidated_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key(), javaHome + "/lib/security/cacerts");
        localConfig.save();
        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Assertions.assertEquals(SmimeHandlerImpl.getKeyStore(), SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getKeyStore_when_store_is_valid_and_cache_is_invalidated_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key(), javaHome + "/lib/security/cacerts");
        localConfig.save();
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Field trustStoreRefreshTime = SmimeHandlerImpl.class.getDeclaredField("trustStoreRefreshTime");
        trustStoreRefreshTime.setAccessible(true);
        KeyStore keyStore = SmimeHandlerImpl.getKeyStore();
        trustStoreRefreshTime.set(null, 0);
        Assertions.assertNotEquals(keyStore, SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getX509TrustManager_when_holders_is_empty_then_returns_empty_list() throws Exception {
        SMIMESigned signed = Mockito.mock();
        Store<X509CertificateHolder> holders = Mockito.mock();
        Mockito.when(signed.getCertificates()).thenReturn(holders);
        Mockito.when(holders.getMatches(null)).thenReturn(List.of());
        Assertions.assertTrue(SmimeHandlerImpl.getX509Certificates(signed).isEmpty());
    }

    @Test
    void test_getX509TrustManager_when_holders_is_not_empty_then_returns_list() throws Exception {
        SMIMESigned signed = Mockito.mock();
        Store<X509CertificateHolder> holders = Mockito.mock();
        Mockito.when(signed.getCertificates()).thenReturn(holders);
        X509CertificateHolder x509CertificateHolder = Mockito.mock();
        Mockito.when(x509CertificateHolder.getEncoded()).thenReturn(certificate.getEncoded());
        Mockito.when(holders.getMatches(null)).thenReturn(List.of(x509CertificateHolder));
        Assertions.assertEquals(1, SmimeHandlerImpl.getX509Certificates(signed).size());
    }

    @Test
    void test_verifyMessageSignature_when_isCarbonioSMIMESignatureVerificationEnabled_is_false_then_return_false() {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        Mockito.when(server.isCarbonioSMIMESignatureVerificationEnabled()).thenReturn(false);
        Assertions.assertFalse(smimeHandler.verifyMessageSignature(Mockito.mock(),
                new Element.JSONElement("test"),
                message, Mockito.mock(OperationContext.class)));
    }

    @Test
    void test_verifyMessageSignature_when_isCarbonioSMIMESignatureVerificationEnabled_is_true_and_cacert_does_exist_then_return_false()
            throws Exception {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key(), "cacerts_not_found");
        localConfig.save();
        Mockito.when(server.isCarbonioSMIMESignatureVerificationEnabled()).thenReturn(true);
        Element.JSONElement element = new Element.JSONElement("test");
        Assertions.assertFalse(smimeHandler.verifyMessageSignature(Mockito.mock(),
                element,
                message, Mockito.mock(OperationContext.class)));
        System.out.println(element);
    }

    @Test
    void test_verifyMessageSignature_when_isCarbonioSMIMESignatureVerificationEnabled_is_true_and_cacert_exist_but_issuer_is_untrusted_then_return_false()
            throws Exception {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        Files.copy(Path.of(System.getProperty("java.home") + "/lib/security/cacerts"), Path.of("cacerts"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        localConfig.set(LC.mailboxd_truststore.key(), "cacerts");
        localConfig.save();
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Mockito.when(server.isCarbonioSMIMESignatureVerificationEnabled()).thenReturn(true);
        Element.JSONElement element = new Element.JSONElement("test");
        com.zimbra.cs.mailbox.Message msg = Mockito.mock();
        Mockito.when(msg.getSender()).thenReturn("user@demo.zextras.io");
        Assertions.assertFalse(smimeHandler.verifyMessageSignature(msg,
                element,
                SmimeHandlerImplTest.message, Mockito.mock(OperationContext.class)));
        Assertions.assertEquals("UNTRUSTED", element.getElement("signature").getAttribute("messageCode"));
        System.out.println(element);
    }

    @Test
    void test_verifyMessageSignature_when_isCarbonioSMIMESignatureVerificationEnabled_is_true_and_cacert_exist_but_issuer_is_trusted_then_return_true()
            throws Exception {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        Files.copy(Path.of(System.getProperty("java.home") + "/lib/security/cacerts"), Path.of("cacerts_trusted"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        localConfig.set(LC.mailboxd_truststore.key(), "cacerts_trusted");
        localConfig.save();
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Mockito.when(server.isCarbonioSMIMESignatureVerificationEnabled()).thenReturn(true);
        Element.JSONElement element = new Element.JSONElement("test");
        com.zimbra.cs.mailbox.Message msg = Mockito.mock();
        Mockito.when(msg.getSender()).thenReturn("user@demo.zextras.io");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream("cacerts_trusted")) {
            keystore.load(fis, "changeit".toCharArray());
        }
        keystore.setCertificateEntry("my-ca", issuerCert);
        var alias = "my-ca";
        if (keystore.containsAlias(alias)) {
            System.out.println("Alias '" + alias + "' found in cacerts.");
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
            System.out.println("Certificate for alias '" + alias + "':");
            System.out.println(cert);
        } else {
            System.out.println("Alias '" + alias + "' not found in cacerts.");
        }
        // Save the updated keystore
        try (FileOutputStream fos = new FileOutputStream("cacerts_trusted")) {
            keystore.store(fos, "changeit".toCharArray());
        }
        keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream("cacerts_trusted")) {
            keystore.load(fis, "changeit".toCharArray());
        }
        if (keystore.containsAlias(alias)) {
            System.out.println("Alias '" + alias + "' found in cacerts.");
            X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);
            System.out.println("Certificate for alias '" + alias + "':");
            System.out.println(cert);
        } else {
            System.out.println("Alias '" + alias + "' not found in cacerts.");
        }
        boolean condition = smimeHandler.verifyMessageSignature(msg,
                element,
                SmimeHandlerImplTest.message, Mockito.mock(OperationContext.class));
        System.out.println(element);
        Assertions.assertTrue(condition);
        Assertions.assertEquals("VALID", element.getElement("signature").getAttribute("messageCode"));
        System.out.println(element);
    }

}
