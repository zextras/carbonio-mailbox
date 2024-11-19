package com.zimbra.soap.account.type;

import com.zimbra.common.soap.SignatureConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class MailSignature {

    /**
     * @zm-api-field-tag valid
     * @zm-api-field-description Valid
     */
    @XmlAttribute(name= SignatureConstants.VALID /* valid*/, required=true)
    private boolean valid;

    /**
     * @zm-api-field-tag message
     * @zm-api-field-description Message
     */
    @XmlAttribute(name= SignatureConstants.MESSAGE /* message */, required=true)
    private String message;

    /**
     * @zm-api-field-tag messageCode VALID, INVALID, UNTRUSTED, SIGNER_CERT_EXPIRED, SIGNER_CERT_NOT_FOUND, ISSUER_CERT_NOT_FOUND, ERROR
     * @zm-api-field-description SignatureConstants.MessageCodeEnum VALID, INVALID, UNTRUSTED, SIGNER_CERT_EXPIRED, SIGNER_CERT_NOT_FOUND, ISSUER_CERT_NOT_FOUND, ERROR
     */
    @XmlAttribute(name= SignatureConstants.MESSAGE_CODE /* messageCode */, required=true)
    private String messageCode;

    /**
     * @zm-api-field-tag type S/MIME, PGP
     * @zm-api-field-description type S/MIME, PGP
     */
    @XmlAttribute(name= SignatureConstants.TYPE /* type */, required=true)
    private String type;

    /**
     * @zm-api-field-tag email
     * @zm-api-field-description email
     */
    @XmlAttribute(name= SignatureConstants.EMAIL /* email*/, required=true)
    private String email;

    /**
     * @zm-api-field-tag notBefore
     * @zm-api-field-description notBefore
     */
    @XmlAttribute(name= SignatureConstants.NOT_BEFORE /* notBefore*/, required=true)
    private Long notBefore;

    /**
     * @zm-api-field-tag notBefore
     * @zm-api-field-description notAfter
     */
    @XmlAttribute(name= SignatureConstants.NOT_AFTER /* notAfter*/, required=true)
    private Long notAfter;

    /**
     * @zm-api-field-tag issuer
     * @zm-api-field-description issuer
     */
    @XmlAttribute(name= SignatureConstants.ISSUER /* issuer*/, required=true)
    private String issuer;

    /**
     * @zm-api-field-tag trusted
     * @zm-api-field-description trusted
     */
    @XmlAttribute(name= SignatureConstants.TRUSTED /* trusted*/, required=true)
    private boolean trusted;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    public Long getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Long notAfter) {
        this.notAfter = notAfter;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }


    public MailSignature() {}

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
