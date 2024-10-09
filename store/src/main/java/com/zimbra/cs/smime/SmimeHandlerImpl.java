package com.zimbra.cs.smime;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.i18n.ErrorBundle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.validator.SignedMailValidator;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SmimeHandlerImpl extends SmimeHandler {
    private static final Log LOG = ZimbraLog.smime;

    private static final String RESOURCE_NAME = "org.bouncycastle.mail.smime.validator.SignedMailValidatorMessages";
    public static final int TITLE = 0;
    public static final int TEXT = 1;
    public static final int SUMMARY = 2;
    public static final int DETAIL = 3;
    public static final String SMIME_SIGNATURE_VALID = "smime_signature_valid";
    public static final String SMIME_ERROR = "smime_error";
    public static final String CERTIFICATE_AUTHORITY_VALID = "smime_authority_valid";
    static int dbgLvl = DETAIL;

    @Override
    public boolean verifyMessageSignature(Message msg, Element element, MimeMessage mimeMessage, OperationContext octxt) {
        try {
            if (mimeMessage.isMimeType("multipart/signed")) {
                // Extract and validate the signature
                MimeMultipart content = (MimeMultipart) mimeMessage.getContent();
                SMIMESigned signed = new SMIMESigned(content);

                // Get the certificates from the signed email
                Collection<X509CertificateHolder> certHolders = signed.getCertificates().getMatches(null);
                List<X509Certificate> certList = new ArrayList<>();
                for (X509CertificateHolder certHolder : certHolders) {
                    certList.add(new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certHolder));
                }

                // Assuming the first certificate is the signing certificate
                X509Certificate signingCert = certList.get(0);

                // Load the trust store (containing trusted CA certificates)
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(new FileInputStream(LC.get(LC.mailboxd_truststore.key())), LC.get(LC.mailboxd_truststore_password.key()).toCharArray());

                // Build the certificate chain
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
                CertPath certPath = certFactory.generateCertPath(certList);

                // Set up the PKIX parameters with the trust anchors (trusted root certificates)
                PKIXParameters pkixParams = new PKIXParameters(trustStore);
                pkixParams.setRevocationEnabled(false);  // Set this to true if you want to check for CRLs or OCSP

                // Create a CertPathValidator instance
                CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");

                // Validate the certificate chain (CertPath)
                try {
                    certPathValidator.validate(certPath, pkixParams);
                    element.addAttribute(CERTIFICATE_AUTHORITY_VALID, true);
                } catch (CertPathValidatorException e) {
                    element.addAttribute(CERTIFICATE_AUTHORITY_VALID, false);
                    LOG.warn("Smime Authority certificate is not valid: " + e.getMessage());
                    return false;
                }

                SignedMailValidator validator = new SignedMailValidator(mimeMessage, pkixParams);
                Locale loc = Locale.ENGLISH;
                for (SignerInformation signer : validator.getSignerInformationStore().getSigners()) {
                    SignedMailValidator.ValidationResult result2 = validator
                            .getValidationResult(signer);
                    if (result2.isValidSignature()) {
                        LOG.info("valid");
                    } else {
                        ErrorBundle errMsg = new ErrorBundle(RESOURCE_NAME,
                                "SignedMailValidator.sigInvalid");
                        errMsg.setClassLoader(SignedMailValidator.class.getClassLoader());
                        LOG.error(errMsg.getText(loc));
                        // print errors
                        for (Object o : result2.getErrors()) {
                            ErrorBundle errorMsg = (ErrorBundle) o;
                            if (dbgLvl == DETAIL) {
                                LOG.error(errorMsg.getDetail(loc));
                            } else {
                                LOG.error(errorMsg.getText(loc));
                            }
                        }
                        if (!result2.getErrors().isEmpty()) {
                            element.addAttribute(SMIME_SIGNATURE_VALID, false);
                            return false;
                        }
                    }
                }

            }
            element.addAttribute(SMIME_SIGNATURE_VALID, true);
            return true;

        } catch (Exception e) {
            element.addAttribute(SMIME_SIGNATURE_VALID, false);
            element.addAttribute(SMIME_ERROR, e.getMessage());
        }
        element.addAttribute(SMIME_SIGNATURE_VALID, false);
        return false;
    }

    @Override
    public MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mimeMessage, int itemId) throws ServiceException {
        return mimeMessage;
    }

    @Override
    public void updateCryptoFlags(Message msg, Element element, MimeMessage originalMimeMessage, MimeMessage decryptedMimeMessage) {

    }

    @Override
    public MimeMessage decodePKCS7Message(Account account, MimeMessage pkcs7MimeMessage) {
        return null;
    }

    @Override
    public void addPKCS7SignedMessageSignatureDetails(Account account, Element m, MimeMessage mm, SoapProtocol mResponseProtocol) {

    }

    @Override
    public void encodeCertificate(Account account, Element elem, String certData, SoapProtocol mResponseProtocol, List<String> emailAddresses) {

    }
}
