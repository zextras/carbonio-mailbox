package com.zimbra.cs.smime;
import java.io.ByteArrayInputStream;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;


public class SMIMESignatureVerifier {
  public static boolean verifySMIMESignature(MimeMessage message) throws Exception {
    // Add Bouncy Castle as a security provider
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    // Parse the S/MIME signed message
    SMIMESigned signed = new SMIMESigned(message);
    Store<X509CertificateHolder> certificates = signed.getCertificates();
    SignerInformationStore signers = signed.getSignerInfos();
    Collection<SignerInformation> signerInfos = signers.getSigners();

    for (SignerInformation signer : signerInfos) {
      Collection<X509CertificateHolder> certCollection = certificates.getMatches(signer.getSID());
      if (certCollection.isEmpty()) {
        System.out.println("No matching certificate found for signer.");
        continue;
      }

      X509CertificateHolder certHolder = certCollection.iterator().next();
      X509Certificate certificate = convertCertificate(certHolder);

      try {
        // Create a SignerInformationVerifier using the certificate
        SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
            .setProvider("BC") // Specify Bouncy Castle provider
            .build(certificate);

        // Verify the signature
        if (signer.verify(verifier)) {
          System.out.println("Signature verified successfully.");
          return true;
        } else {
          System.out.println("Signature verification failed.");
        }
      } catch (OperatorCreationException e) {
        System.out.println("Error creating verifier: " + e.getMessage());
      }
    }
    return false;
  }

  public static X509Certificate convertCertificate(X509CertificateHolder certHolder) throws Exception {
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) certFactory.generateCertificate(
        new ByteArrayInputStream(certHolder.getEncoded()));
  }
}
