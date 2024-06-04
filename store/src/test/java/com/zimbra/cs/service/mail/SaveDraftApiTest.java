package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.message.GetMsgResponse;
import com.zimbra.soap.mail.message.SaveDraftRequest;
import com.zimbra.soap.mail.message.SaveDraftResponse;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.MessageInfo;
import com.zimbra.soap.mail.type.MimePartInfo;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.mail.type.MsgToSend;
import com.zimbra.soap.mail.type.MsgWithGroupInfo;
import com.zimbra.soap.mail.type.SaveDraftMsg;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.http.HttpResponse;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class SaveDraftApiTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static GreenMail mta;

  @BeforeAll
  public static void setUp() throws Exception {
    java.security.Security.addProvider(new BouncyCastleProvider());
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);

    mta =
        new GreenMail(
            new ServerSetup[] {
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP),
                new ServerSetup(9000, "127.0.0.1", ServerSetup.PROTOCOL_IMAP)
            });
    mta.start();
  }

  @AfterAll
  public static void tearDown() {
    mta.stop();
  }

  static AuthorityKeyIdentifier createAuthorityKeyId(
      PublicKey pub)
  {
    SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());

    return new AuthorityKeyIdentifier(info);
  }

  static SubjectKeyIdentifier createSubjectKeyId(
      PublicKey pub)
  {
    SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());

    return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
  }

  static X509Certificate makeCertificate(
      KeyPair subKP,
      String  subDN,
      KeyPair issKP,
      String  issDN,
      BigInteger serial)
      throws GeneralSecurityException, IOException, OperatorCreationException
  {
    PublicKey subPub  = subKP.getPublic();
    PrivateKey issPriv = issKP.getPrivate();
    PublicKey  issPub  = issKP.getPublic();

    X509v3CertificateBuilder v3CertGen = new JcaX509v3CertificateBuilder(new X500Name(issDN), serial, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 100)), new X500Name(subDN), subPub);

    v3CertGen.addExtension(
        X509Extension.subjectKeyIdentifier,
        false,
        createSubjectKeyId(subPub));

    v3CertGen.addExtension(
        X509Extension.authorityKeyIdentifier,
        false,
        createAuthorityKeyId(issPub));

    return new JcaX509CertificateConverter().setProvider("BC").getCertificate(v3CertGen.build(new JcaContentSignerBuilder("MD5withRSA").setProvider("BC").build(issPriv)));
  }


  public SMIMESignedGenerator createSMIMESignedGenerator() throws GeneralSecurityException, IOException, OperatorCreationException {
    KeyPairGenerator kpg  = KeyPairGenerator.getInstance("RSA", "BC");

    kpg.initialize(1024);
    InputStream stream = this.getClass().getResourceAsStream("/smime.p12");
    KeyStore store = KeyStore.getInstance("PKCS12");
    store.load(stream, "password".toCharArray());

    kpg.initialize(1024, new SecureRandom());

    //
    // cert that issued the signing certificate (CA Authority, e.g.: Actalis)
    //
    String              signDN = "O=Test, C=IT";
    KeyPair             signKP = kpg.generateKeyPair();
    X509Certificate     signCert =  makeCertificate(
        signKP, signDN, signKP, signDN, BigInteger.valueOf(1));

    //
    // cert we sign against
    // certificato utente
    //
    String              origDN = "CN=Test, E=from@test.com, O=Test, C=IT";
    KeyPair             origKP = kpg.generateKeyPair();
    X509Certificate     x509Certificate = makeCertificate(
        origKP, origDN, signKP, signDN, BigInteger.valueOf(2));

    List                certList = new ArrayList();

    certList.add(x509Certificate);
    certList.add(signCert);

    //
    // create a CertStore containing the certificates we want carried
    // in the signature
    //
    Store certs = new JcaCertStore(certList);

    //
    // create some smime capabilities in case someone wants to respond
    //
    ASN1EncodableVector         signedAttrs = new ASN1EncodableVector();
    SMIMECapabilityVector       caps = new SMIMECapabilityVector();

    caps.addCapability(SMIMECapability.dES_EDE3_CBC);
    caps.addCapability(SMIMECapability.rC2_CBC, 128);
    caps.addCapability(SMIMECapability.dES_CBC);

    signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

    //
    // add an encryption key preference for encrypted responses -
    // normally this would be different from the signing certificate...
    //
    IssuerAndSerialNumber   issAndSer = new IssuerAndSerialNumber(
        new X500Name(signDN), x509Certificate.getSerialNumber());

    signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));

    //
    // create the generator for creating a smime/signed message
    //
    SMIMESignedGenerator gen = new SMIMESignedGenerator();

    //
    // add a signer to the generator - this specifies we are using SHA1 and
    // adding the smime attributes above to the signed attributes that
    // will be generated as part of the signature. The encryption algorithm
    // used is taken from the key - in this RSA with PKCS1Padding
    //
    final PrivateKey privateKey = origKP.getPrivate();
    gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(new AttributeTable(signedAttrs))
        .build(
            "SHA1withRSA",
            privateKey,
            x509Certificate
        )
    );

    //
    // add our pool of certs and cerls (if any) to go with the signature
    //
    gen.addCertificates(certs);
    return gen;
  }


  @Test
  public void signMessage() throws Exception {
    final String password = "password";
    Account testAccount = accountCreatorFactory.get().withPassword(password).create();
    final MsgWithGroupInfo msgWithGroupInfo = createDraftMessage(testAccount);
    String originalMessage = msgWithGroupInfo.getContent().getValue();

    final MimeBodyPart mimeBodyPart = new MimeBodyPart(new ByteArrayInputStream(originalMessage.getBytes(StandardCharsets.UTF_8)));

    var smimeSignedGenerator = createSMIMESignedGenerator();
    MimeMultipart generatedSignedMultiPart = smimeSignedGenerator.generate(mimeBodyPart);
    /* Set all original MIME headers in the signed message */
    MimeMessage signedMessage = new MimeMessage(JMSession.getSmtpSession(testAccount));

    /* Set all original MIME headers in the signed message */
    Enumeration headers = mimeBodyPart.getAllHeaderLines();
    while (headers.hasMoreElements())
    {
      signedMessage.addHeaderLine((String)headers.nextElement());
    }
    /* Set the content of the signed message */
    signedMessage.setContent(generatedSignedMultiPart);
    signedMessage.saveChanges();

    final MsgWithGroupInfo draftSignedMessage = saveDraft(testAccount, msgWithGroupInfo.getId(), signedMessage);

    final HttpResponse sendMsgResponse = sendMessage(testAccount, draftSignedMessage.getId());
    Assertions.assertEquals(200, sendMsgResponse.getStatusLine().getStatusCode());
    final MimeMessage[] receivedMessages = mta.getReceivedMessages();
    Assertions.assertEquals(1, receivedMessages.length);
    final MimeMessage receivedMessage = receivedMessages[0];
    Assertions.assertTrue(receivedMessage.getContentType().contains("multipart/signed"));

  }

  private MsgWithGroupInfo createDraftMessage(Account testAccount) throws Exception {
    final SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
    final SaveDraftMsg msg = new SaveDraftMsg();
    msg.setSubject("Test SMIME email");
    MimePartInfo partInfo = MimePartInfo.createForContentTypeAndContent("text/plain; charset=UTF-8; format=flowed", "hello there");

    msg.setMimePart(partInfo);
    final EmailAddrInfo rcptAddress = new EmailAddrInfo("external@test123.com");
    rcptAddress.setAddressType("t");
    final EmailAddrInfo sender = new EmailAddrInfo(testAccount.getName());
    sender.setAddressType("f");
    msg.setEmailAddresses(List.of(sender, rcptAddress));
    saveDraftRequest.setMsg(msg);

    final MessageInfo draftMsg = getSoapClient().execute(testAccount, saveDraftRequest, SaveDraftResponse.class)
        .getMessage();
    final String draftId = draftMsg.getId();

    return getRawMessage(testAccount, draftId);
  }

  private MsgWithGroupInfo saveDraft(Account testAccount, String draftId, MimeMessage message) throws Exception {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    message.writeTo(byteArrayOutputStream);


//    final SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
//    final SaveDraftMsg msg = new SaveDraftMsg();
//    final String string = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
//    MimePartInfo partInfo = MimePartInfo.createForContentTypeAndContent(message.getContentType(),
//        string);
//    msg.setContent(string);
//    msg.setId(Integer.valueOf(draftId));
//    saveDraftRequest.setMsg(msg);
//
//    final MessageInfo draftMsg = getSoapClient().execute(testAccount, saveDraftRequest, SaveDraftResponse.class)
//        .getMessage();

    final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    final ParsedMessage parsedMessage = new ParsedMessage(message,
        false);
    mailbox.saveDraft(null, parsedMessage, Integer.parseInt(draftId));

    return getMessage(testAccount, draftId);
  }

  private MsgWithGroupInfo getRawMessage(Account testAccount, String draftId) throws Exception {
    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(true);
    return getSoapClient().execute(testAccount,
            new GetMsgRequest(msgSpec), GetMsgResponse.class)
        .getMsg();
  }

  private MsgWithGroupInfo getMessage(Account testAccount, String draftId) throws Exception {
    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(false);
    return getSoapClient().execute(testAccount,
            new GetMsgRequest(msgSpec), GetMsgResponse.class)
        .getMsg();
  }

  private HttpResponse sendMessage(Account testAccount, String draftId) throws Exception {
    final MsgToSend msgToSend = new MsgToSend();

    msgToSend.setDraftId(draftId);
    msgToSend.setSendFromDraft(true);
    final SendMsgRequest soapBodyPOJO = new SendMsgRequest();
    soapBodyPOJO.setMsg(msgToSend);
    return getSoapClient().executeSoap(testAccount,
            soapBodyPOJO);
  }

}