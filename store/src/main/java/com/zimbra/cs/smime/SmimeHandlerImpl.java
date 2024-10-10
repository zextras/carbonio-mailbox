package com.zimbra.cs.smime;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SignatureConstants;
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
import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
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
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final Log LOG = ZimbraLog.smime;

    private static final String RESOURCE_NAME = "org.bouncycastle.mail.smime.validator.SignedMailValidatorMessages";
    public static final int DETAIL = 3;
    static int dbgLvl = DETAIL;

    /**
     * Verify the signature of the message
     * {
     *  "signature": {
     *             "valid": false,
     *             "message": "issuer is not trusted",
     *             "certificate": {
     *               "email": "email@xx.com",
     *               "notBefore": 12321312312,
     *               "notAfter": 12321231,
     *               "issuer": {
     *                 "trusted": false,
     *                 "name": "Actalis"
     *               }
     *             },
     *           }
     */
    @Override
    public boolean verifyMessageSignature(Message msg, Element element, MimeMessage mimeMessage, OperationContext octxt) {
        Element signatureElement = null;
        try {
            Element optionalElement = element.getOptionalElement(SignatureConstants.SIGNATURE);
            if (optionalElement != null) {
                return optionalElement.getAttributeBool(SignatureConstants.VALID, false);
            }
            if (mimeMessage.isMimeType("multipart/signed")) {
                // Extract and validate the signature
                MimeMultipart content = (MimeMultipart) mimeMessage.getContent();

                SMIMESigned signed = new SMIMESigned(content);
                element.addUniqueElement(SignatureConstants.SIGNATURE).addUniqueElement(SignatureConstants.CERTIFICATE).addUniqueElement(SignatureConstants.ISSUER);
                signatureElement = element.getElement(SignatureConstants.SIGNATURE);
                        Element signerCert = element.getElement(SignatureConstants.SIGNATURE).getElement(SignatureConstants.CERTIFICATE);
                Element issuerElement = element.getElement(SignatureConstants.SIGNATURE).getElement(SignatureConstants.CERTIFICATE).getElement(SignatureConstants.ISSUER);
                // Get the certificates from the signed email
                Collection<X509CertificateHolder> certHolders = signed.getCertificates().getMatches(null);
                List<X509Certificate> certList = new ArrayList<>();
                for (X509CertificateHolder certHolder : certHolders) {
                    X509Certificate certificate = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certHolder);
                    certList.add(certificate);

                }

                // Load the trust store (containing trusted CA certificates)
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(new FileInputStream(LC.get(LC.mailboxd_truststore.key())), LC.get(LC.mailboxd_truststore_password.key()).toCharArray());

                // Build the certificate chain
                CertificateFactory certFactory = CertificateFactory.getInstance(SignatureConstants.X_509, new BouncyCastleProvider());
                CertPath certPath = certFactory.generateCertPath(certList);

                // Set up the PKIX parameters with the trust anchors (trusted root certificates)
                PKIXParameters pkixParams = new PKIXParameters(trustStore);
                pkixParams.setRevocationEnabled(false);  // Set this to true if you want to check for CRLs or OCSP

                // Create a CertPathValidator instance
                CertPathValidator certPathValidator = CertPathValidator.getInstance(SignatureConstants.PKIX);

                // Validate the certificate chain (CertPath)
                try {
                    certPathValidator.validate(certPath, pkixParams);
                    issuerElement.addAttribute(SignatureConstants.TRUSTED, true);
                } catch (CertPathValidatorException e) {
                    issuerElement.addAttribute(SignatureConstants.TRUSTED, false);
                    signatureElement.addAttribute(SignatureConstants.MESSAGE, "issuer is not trusted. Detail: " + e.getMessage());
                    certList.forEach(certificate -> {
                        if(msg.getSender().equals(extractCN(certificate.getSubjectX500Principal()))) {
                            signerCert.addAttribute(SignatureConstants.EMAIL, extractCN(certificate.getSubjectX500Principal()));
                            signerCert.addAttribute(SignatureConstants.NOT_BEFORE, certificate.getNotBefore().getTime());
                            signerCert.addAttribute(SignatureConstants.NOT_AFTER, certificate.getNotAfter().getTime());
                            issuerElement.addAttribute(SignatureConstants.NAME, extractCN(certificate.getIssuerX500Principal()));
                        }
                    });
                    LOG.warn("Smime Authority certificate is not valid: " + e.getMessage());
                    return false;
                }

                SignedMailValidator validator = new SignedMailValidator(mimeMessage, pkixParams);
                Locale loc = Locale.ENGLISH;
                for (SignerInformation signer : validator.getSignerInformationStore().getSigners()) {
                    certList.forEach(certificate -> {
                        if (signer.getSID().getSerialNumber().equals(certificate.getSerialNumber())) {
                            signerCert.addAttribute(SignatureConstants.EMAIL, extractCN(certificate.getSubjectX500Principal()));
                            signerCert.addAttribute(SignatureConstants.NOT_BEFORE, certificate.getNotBefore().getTime());
                            signerCert.addAttribute(SignatureConstants.NOT_AFTER, certificate.getNotAfter().getTime());
                            issuerElement.addAttribute(SignatureConstants.NAME, extractCN(certificate.getIssuerX500Principal()));
                        }
                    });
                    SignedMailValidator.ValidationResult validationResult = validator.getValidationResult(signer);
                    if (validationResult.isValidSignature()) {
                        signatureElement.addAttribute(SignatureConstants.MESSAGE, "Valid");
                        LOG.info("valid");
                    } else {
                        ErrorBundle errMsg = new ErrorBundle(RESOURCE_NAME,
                                "SignedMailValidator.sigInvalid");
                        errMsg.setClassLoader(SignedMailValidator.class.getClassLoader());
                        String error = errMsg.getText(loc);
                        LOG.error(error);
                        // print errors
                        for (Object o : validationResult.getErrors()) {
                            ErrorBundle errorMsg = (ErrorBundle) o;
                            if (dbgLvl == DETAIL) {
                                LOG.error(errorMsg.getDetail(loc));
                                signatureElement.addAttribute(SignatureConstants.MESSAGE, error + " " + errorMsg.getDetail(loc));
                            } else {
                                LOG.error(errorMsg.getText(loc));
                                signatureElement.addAttribute(SignatureConstants.MESSAGE, error + " " + errorMsg.getText(loc));
                            }
                        }
                        if (!validationResult.getErrors().isEmpty()) {
                            signatureElement.addAttribute(SignatureConstants.VALID, false);
                            return false;
                        }
                    }
                    signatureElement.addAttribute(SignatureConstants.VALID, true);
                    return true;
                }
            }

        } catch (Exception e) {
            LOG.error(e);
            if (signatureElement != null) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE, e.getMessage());
            }
        }

        if (signatureElement != null) {
            signatureElement.addAttribute(SignatureConstants.VALID, false);
        }
        return false;
    }

    private String extractCN(X500Principal principal) {
        String name = principal.getName();
        String[] parts = name.split(",");
        for (String part : parts) {
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return null;
    }

    @Override
    public MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mimeMessage, int itemId) {
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
