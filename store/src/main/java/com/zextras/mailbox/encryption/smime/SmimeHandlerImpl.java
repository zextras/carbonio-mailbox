// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.encryption.smime;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SignatureConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.smime.SmimeHandler;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.pkix.util.ErrorBundle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.validator.SignedMailValidator;
import org.bouncycastle.mail.smime.validator.SignedMailValidatorException;

import java.security.NoSuchProviderException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * This class is responsible for handling S/MIME messages.
 */
public class SmimeHandlerImpl extends SmimeHandler {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static final int CACHE_TIMEOUT_MILLIS = 600000;
    private static final Log LOG = ZimbraLog.smime;
    private static final String RESOURCE_NAME = "org.bouncycastle.mail.smime.validator.SignedMailValidatorMessages";
    private static final Lock LOCK = new ReentrantLock();
    private static long trustStoreRefreshTime = 0L;
    private static KeyStore trustStore = null;

    /**
     *
     * Verify the signature of the message
     * <pre>
     * {
     * "signature":
     *   [
     *      {
     *          "type": "S/MIME",
     *          "valid": false,
     *          "message": "issuer is not trusted",
     *          "messageCode": "UNTRUSTED",
     *          "email": "email@xx.com",
     *          "notBefore": 12321312312,
     *          "notAfter": 12321231,
     *          "issuer": "issuer name":
     *          "trusted": false,
     *      }
     *   ]
     * } <br>
     * </pre>
     *
     *  public key: RSA public key: generated by the private key <br>
     *  private key: generated by you. You should not share with any other person <br>
     *  certificate not exactly but like signed public key certificate is created from public key <br>
     *  Signer certificate email sender certificate issuer  <br>
     *  issuer: Authorities of the certificate These issuers create Certificates by issuer private key. <br>
     *  this means signed certificate is valid. only check validity date is enough. <br>
     */
    @Override
    public boolean verifyMessageSignature(Message msg, Element element, MimeMessage mimeMessage, OperationContext octxt) {

        Element signatureElement = null;
        try {
            if (!signatureEnabled()) {
                return false;
            }

            MimeMultipart content = (MimeMultipart) mimeMessage.getContent();
            SMIMESigned signed = new SMIMESigned(content);
            Element dummy = element.addNonUniqueElement(SignatureConstants.SIGNATURE);
            signatureElement = element.addNonUniqueElement(SignatureConstants.SIGNATURE);
            signatureElement.addAttribute(SignatureConstants.TYPE, SignatureConstants.SignatureType.SMIME.value());
            signatureElement.addAttribute(SignatureConstants.TRUSTED, false);
            signatureElement.addAttribute(SignatureConstants.VALID, false);
            dummy.detach();
            List<X509Certificate> certList = getX509Certificates(signed);
            PKIXParameters pkixParams = new PKIXParameters(getKeyStore());
            pkixParams.setRevocationEnabled(false);  // Set this to true if you want to check for CRLs or OCSP
            SignedMailValidator validator = new SignedMailValidator(mimeMessage, pkixParams);
            Optional<X509Certificate> signerCertificate = getSignerCertificate(certList, validator);

            if (signerCertificate.isEmpty()) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE, "Cannot find signer certificate");
                signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                        SignatureConstants.MessageCodeEnum.SIGNER_CERT_NOT_FOUND.toString());
                return false;
            }

            X509Certificate signerCert = signerCertificate.get();
            setCertDetails(signerCert, signatureElement);
            Optional<X509Certificate> issuerCertificate = getIssuerCertificate(certList, signerCert);

            if (issuerCertificate.isEmpty()) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE, "Cannot find issuer certificate");
                signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                        SignatureConstants.MessageCodeEnum.ISSUER_CERT_NOT_FOUND.toString());
                return false;
            }

            return validateIssuerCertificate(pkixParams, signatureElement, issuerCertificate.get())
                    && validateSignature(pkixParams, signerCert, signatureElement, validator);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            if (signatureElement != null) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE, e.getMessage());
                signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                        SignatureConstants.MessageCodeEnum.ERROR.toString());
            }

        }

        return false;
    }

    @Override
    public boolean signatureEnabled() {

        try {
            return Provisioning.getInstance().getLocalServer().isCarbonioSMIMESignatureVerificationEnabled();

        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mimeMessage, int itemId) throws ServiceException {
        return mimeMessage;
    }

    @Override
    public void updateCryptoFlags(Message msg, Element element, MimeMessage originalMimeMessage,
                                  MimeMessage decryptedMimeMessage) {
    }

    @Override
    public MimeMessage decodePKCS7Message(Account account, MimeMessage pkcs7MimeMessage) {
        return null;
    }


    @Override
    public void encodeCertificate(Account account, Element elem, String certData, SoapProtocol mResponseProtocol,
                                  List<String> emailAddresses) {
    }

    private boolean validateIssuerCertificate(PKIXParameters pkixParams, Element signatureElement, X509Certificate certificate)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, CertificateException, NoSuchProviderException {

        CertPathValidator certPathValidator = CertPathValidator.getInstance(SignatureConstants.PKIX);

        CertPath certPath = CertificateFactory.getInstance(SignatureConstants.X_509, BouncyCastleProvider.PROVIDER_NAME)
                .generateCertPath(List.of(certificate));

        try {
            certPathValidator.validate(certPath, pkixParams);
            signatureElement.addAttribute(SignatureConstants.TRUSTED, true);
            return true;

        } catch (CertPathValidatorException e) {
            signatureElement.addAttribute(SignatureConstants.MESSAGE, "issuer is not trusted. Detail: " + e.getMessage());
            signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE, SignatureConstants.MessageCodeEnum.UNTRUSTED.toString());
            LOG.warn("Smime Authority certificate is not valid: " + e.getMessage());
            return false;
        }

    }

    private Optional<X509Certificate> getIssuerCertificate(List<X509Certificate> certList,  X509Certificate signerCertificate) {
        return certList.stream()
                .filter(certificate -> signerCertificate.getIssuerX500Principal().equals(certificate.getSubjectX500Principal()))
                .findFirst();
    }

    private Optional<X509Certificate> getSignerCertificate(List<X509Certificate> certList, SignedMailValidator validator) {
        for (SignerInformation signer : validator.getSignerInformationStore().getSigners()){
            return certList.stream()
                    .filter(certificate -> signer.getSID().getSerialNumber().equals(certificate.getSerialNumber()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private boolean validateSignature(PKIXParameters pkixParams, X509Certificate signerCertificate, Element signatureElement, SignedMailValidator validator)
            throws SignedMailValidatorException {

        for (SignerInformation signer : validator.getSignerInformationStore().getSigners()) {

            if (signerCertificate.getNotAfter().getTime() < System.currentTimeMillis()) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE,
                        SignatureConstants.MessageCodeEnum.SIGNER_CERT_EXPIRED.toString());
                signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                        SignatureConstants.MessageCodeEnum.SIGNER_CERT_EXPIRED.toString());
                return false;
            }

            SignedMailValidator.ValidationResult validationResult = validator.getValidationResult(signer);

            if (validationResult.isValidSignature()) {
                signatureElement.addAttribute(SignatureConstants.MESSAGE, SignatureConstants.VALID);
                signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                        SignatureConstants.MessageCodeEnum.VALID.toString());
                LOG.debug("valid");

            } else {
                ErrorBundle errMsg = new ErrorBundle(RESOURCE_NAME, "SignedMailValidator.sigInvalid");
                errMsg.setClassLoader(SignedMailValidator.class.getClassLoader());
                String error = errMsg.getText(Locale.ENGLISH);
                LOG.error(error);

                for (Object validationError : validationResult.getErrors()) {

                    if (validationError instanceof ErrorBundle errorMsg) {
                        LOG.error(errorMsg.getDetail(Locale.ENGLISH));
                        signatureElement.addAttribute(SignatureConstants.MESSAGE, error + " " + errorMsg.getDetail(Locale.ENGLISH));

                    } else {
                        LOG.error(validationError.toString());
                        signatureElement.addAttribute(SignatureConstants.MESSAGE, error + " " + validationError);
                    }

                    signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                            SignatureConstants.MessageCodeEnum.INVALID.toString());
                    return false;
                }
            }

            signatureElement.addAttribute(SignatureConstants.VALID, true);
            signatureElement.addAttribute(SignatureConstants.MESSAGE, SignatureConstants.VALID);
            signatureElement.addAttribute(SignatureConstants.MESSAGE_CODE,
                    SignatureConstants.MessageCodeEnum.VALID.toString());
            return true;
        }

       return false;
    }

    private void setCertDetails(X509Certificate certificate, Element signatureElement) {
        signatureElement.addAttribute(SignatureConstants.EMAIL, extractCN(certificate.getSubjectX500Principal()));
        signatureElement.addAttribute(SignatureConstants.NOT_BEFORE, certificate.getNotBefore().getTime());
        signatureElement.addAttribute(SignatureConstants.NOT_AFTER, certificate.getNotAfter().getTime());
        signatureElement.addAttribute(SignatureConstants.ISSUER, extractCN(certificate.getIssuerX500Principal()));
    }

    static List<X509Certificate> getX509Certificates(SMIMESigned signed) throws CertificateException {
        Collection<X509CertificateHolder> certHolders = signed.getCertificates().getMatches(null);
        List<X509Certificate> certList = new ArrayList<>();
        JcaX509CertificateConverter jcaX509CertificateConverter = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);

        for (X509CertificateHolder certHolder : certHolders) {
            X509Certificate certificate = jcaX509CertificateConverter.getCertificate(certHolder);
            certList.add(certificate);
        }

        return certList;
    }

    static KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        if (trustStore == null || (System.currentTimeMillis() - trustStoreRefreshTime > CACHE_TIMEOUT_MILLIS)) {
            LOCK.lock();  // Acquires the lock

            try {

                if (trustStore == null || (System.currentTimeMillis() - trustStoreRefreshTime > CACHE_TIMEOUT_MILLIS)) {
                    trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

                    try (FileInputStream fis = new FileInputStream(LC.get(LC.mailboxd_truststore.key()))) {
                        trustStore.load(fis,
                                LC.get(LC.mailboxd_truststore_password.key()).toCharArray());
                        trustStoreRefreshTime = System.currentTimeMillis();
                    }

                }

            } finally {
                LOCK.unlock();  // Releases the lock
            }
        }

        return trustStore;
    }

    String extractCN(X500Principal principal) {
        String name = principal.getName();
        String[] parts = name.split(",");

        for (String part : parts) {

            if (part.toUpperCase().startsWith("CN=")) {
                return part.substring(3);
            }

        }

        return "";
    }
}
