package com.zimbra.cs.mailclient.smtp;

import com.zimbra.common.service.ServiceException;
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
import java.util.List;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

public class SMIMETransport extends SmtpTransport {


  public SMIMETransport(Session session, URLName urlname) {
    super(session, urlname);
  }

  private SMIMESignedGenerator createSMIMESignedGenerator(String accountName)
      throws GeneralSecurityException, IOException, OperatorCreationException {
    KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
    // TODO: certifcate must be loaded based on accountName

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

  private Message signMessage(Message message, String accountName)
      throws ServiceException, MessagingException, IOException, GeneralSecurityException, OperatorCreationException, SMIMEException {
    final MimeMessage mimeMessage = new MimeMessage(this.session,
        new ByteArrayInputStream(message.getInputStream()
            .readAllBytes()));
    final MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(
        mimeMessage.getContent(),
        mimeMessage.getDataHandler().getContentType()
    );

    var smimeSignedGenerator = createSMIMESignedGenerator(accountName);
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
      if (shouldSignMessage(msg)) {
        final String accountName = this.session.getProperty("mail.smtp.from");
        msg = signMessage(msg, accountName);
      }
      super.sendMessage(msg, addresses);
    } catch (ServiceException | IOException | GeneralSecurityException |
             OperatorCreationException | SMIMEException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean shouldSignMessage(Message msg) throws MessagingException {
    final String[] toSign = msg.getHeader("X-Sign");
    return toSign != null && toSign.length > 0 && toSign[0].equalsIgnoreCase("true");
  }
}