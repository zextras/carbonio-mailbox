package com.zimbra.cs.service.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mailclient.smtp.SmtpTransport;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.*;
import com.zimbra.soap.mail.type.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeBodyPart;
import org.apache.http.HttpResponse;
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
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.*;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.HashMap;
import java.util.List;

@Tag("api")
public class SaveDraftApiTest extends SoapTestSuite {

  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static GreenMail greenMailAgent;

  @BeforeAll
  public static void setUp() throws Exception {
    LC.smtp_transport_class.setDefault("com.zimbra.cs.mailclient.smtp.SMIMETransport");
    java.security.Security.addProvider(new BouncyCastleProvider());
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);

    greenMailAgent =
        new GreenMail(
            new ServerSetup[]{
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP),
                new ServerSetup(9000, "127.0.0.1", ServerSetup.PROTOCOL_IMAP)
            });
    greenMailAgent.start();
  }

  @AfterAll
  public static void tearDown() {
    greenMailAgent.stop();
  }


  @Test
  public void sendSignedMessage() throws Exception {
    final String password = "password";
    provisioning.createDomain("demo.zextras.io", new HashMap<>());
    Account sender = accountCreatorFactory.get()
        .withUsername("test.smime")
        .withDomain("demo.zextras.io")
        .withPassword(password).create();

    final MsgWithGroupInfo draftMessage = createDraftMessage(sender);
    markMessageReadyToSign(sender, draftMessage.getId());

    final HttpResponse sendMsgResponse = sendMessage(sender, draftMessage.getId());
    final SendMsgResponse responseBody = JaxbUtil.elementToJaxb(
        Element.parseXML(new String(sendMsgResponse.getEntity().getContent().readAllBytes()))
            .getElement("Body").listElements().get(0)
    );

    Assertions.assertEquals(200, sendMsgResponse.getStatusLine().getStatusCode());

    final MimeMessage[] receivedMessages = greenMailAgent.getReceivedMessages();
    Assertions.assertEquals(1, receivedMessages.length);
    final MimeMessage receivedMessage = receivedMessages[0];
    Assertions.assertTrue(receivedMessage.getContentType().contains("multipart/signed"));

    MsgWithGroupInfo rawMessage = getRawMessage(sender, responseBody.getMsg().getId());
    System.out.println(rawMessage.getContent().getValue());
  }

  private void markMessageReadyToSign(Account testAccount, String draftId) throws Exception {
    String rawMessage = getRawMessage(testAccount, draftId).getContent().getValue();
    final MimeMessage originalMessage = new MimeMessage(null,
        new ByteArrayInputStream(rawMessage.getBytes()));
    originalMessage.addHeader("X-Sign", "true");
    originalMessage.saveChanges();
    final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);
    final ParsedMessage parsedMessage = new ParsedMessage(
        originalMessage,
        false);
    mailbox.saveDraft(null, parsedMessage, Integer.parseInt(draftId));
  }

  @Test
  public void ifSendFailsMessageIsNotStoredSigned() throws Exception {
    final String password = "password";
    provisioning.createDomain("demo.zextras.io", new HashMap<>());
    Account sender = accountCreatorFactory.get()
        .withUsername("test.smime")
        .withDomain("demo.zextras.io")
        .withPassword(password).create();

    final MsgWithGroupInfo draftMessage = createDraftMessageToAddress(sender, "");
    final HttpResponse sendMsgResponse = sendMessage(sender, draftMessage.getId());

    Assertions.assertEquals(500, sendMsgResponse.getStatusLine().getStatusCode());
    final MsgWithGroupInfo draftMessageWithoutChanges = getRawMessage(sender, draftMessage.getId());
    Assertions.assertFalse(
        draftMessageWithoutChanges.getContent()
        .getValue().contains("multipart/signed")
    );
  }

  private MsgWithGroupInfo createDraftMessage(Account testAccount) throws Exception {
    return createDraftMessageToAddress(testAccount, "external@test123.com");
  }

  private MsgWithGroupInfo createDraftMessageToAddress(Account testAccount, String toAddress) throws Exception {
    final SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
    final SaveDraftMsg msg = new SaveDraftMsg();
    msg.setSubject("Test SMIME email");
    MimePartInfo partInfo = MimePartInfo.createForContentTypeAndContent(
        "text/plain; charset=UTF-8; format=flowed", "hello there");

    msg.setMimePart(partInfo);
    final EmailAddrInfo rcptAddress = new EmailAddrInfo(toAddress);
    rcptAddress.setAddressType("t");
    final EmailAddrInfo sender = new EmailAddrInfo(testAccount.getName());
    sender.setAddressType("f");
    msg.setEmailAddresses(List.of(sender, rcptAddress));
    saveDraftRequest.setMsg(msg);

    final MessageInfo draftMsg = getSoapClient().execute(testAccount, saveDraftRequest,
            SaveDraftResponse.class)
        .getMessage();
    final String draftId = draftMsg.getId();

    return getRawMessage(testAccount, draftId);
  }

  private MsgWithGroupInfo getRawMessage(Account testAccount, String draftId) throws Exception {
    final MsgSpec msgSpec = new MsgSpec(draftId);
    msgSpec.setRaw(true);
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

  public static class SMIMETransport extends SmtpTransport {


    public SMIMETransport(Session session, URLName urlname) {
      super(session, urlname);
    }

    private SMIMESignedGenerator createSMIMESignedGenerator()
        throws GeneralSecurityException, IOException, OperatorCreationException {
      KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
      final char[] certPassword = "password".toCharArray();
      final InputStream certificateStream = this.getClass().getClassLoader()
          .getResourceAsStream("smime_test_user.p12");
      keystore.load(certificateStream, certPassword);
      final String alias = keystore.aliases().nextElement();
      PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, certPassword);
      Certificate[] chain = keystore.getCertificateChain(alias);

      /* Create the SMIMESignedGenerator */
      SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
      capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
      capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
      capabilities.addCapability(SMIMECapability.dES_CBC);

      ASN1EncodableVector attributes = new ASN1EncodableVector();
      final X509Certificate x509Certificate = (X509Certificate) chain[0];
      final SMIMEEncryptionKeyPreferenceAttribute asn1Encodable = new SMIMEEncryptionKeyPreferenceAttribute(
          new IssuerAndSerialNumber(
              new X500Name(x509Certificate
                  .getIssuerDN().getName()),
              x509Certificate.getSerialNumber()));
      attributes.add(asn1Encodable);
      attributes.add(new SMIMECapabilitiesAttribute(capabilities));

      if (!privateKey.getAlgorithm().equals("RSA")) {
        throw new RuntimeException(
            "Private key algorithm not handled: " + privateKey.getAlgorithm());
      }

      SMIMESignedGenerator signer = new SMIMESignedGenerator();
      signer.addSignerInfoGenerator(
          new JcaSimpleSignerInfoGeneratorBuilder()
              .setProvider("BC").setSignedAttributeGenerator(new AttributeTable(attributes))
              .build("SHA256withRSA", privateKey, x509Certificate)
      );

      /* Add the list of certs to the generator */
      List certList = new ArrayList();
      certList.add(chain[0]);
      Store certs = new JcaCertStore(certList);
      signer.addCertificates(certs);
      return signer;
    }

    private Message signMessage(Message message)
        throws ServiceException, MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException {
      final MimeMessage mimeMessage = new MimeMessage(this.session,
          new ByteArrayInputStream(message.getInputStream()
              .readAllBytes()));
      final MimeBodyPart mimeBodyPart = new MimeBodyPart();
      mimeBodyPart.setContent(
          mimeMessage.getContent(),
          mimeMessage.getDataHandler().getContentType()
      );

      var smimeSignedGenerator = createSMIMESignedGenerator();
      MimeMultipart generatedSignedMultiPart = smimeSignedGenerator.generate(mimeBodyPart);
      MimeMessage signedMessage = new MimeMessage(this.session);
      Enumeration headers = mimeMessage.getAllHeaderLines();
      while (headers.hasMoreElements()) {
        signedMessage.addHeaderLine((String) headers.nextElement());
      }
      /* Set the content of the signed message */
      signedMessage.setContent(generatedSignedMultiPart);
      signedMessage.saveChanges();
      return signedMessage;
    }

    @Override
    public void sendMessage(javax.mail.Message msg, Address[] addresses) throws MessagingException {
      try {
        Message signedMessage = signMessage(msg);
        super.sendMessage(signedMessage, addresses);
      } catch (ServiceException | IOException | GeneralSecurityException |
               OperatorCreationException | SMIMEException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

