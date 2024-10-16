package com.zimbra.soap.account.type;

import com.zimbra.common.soap.SignatureConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
    @XmlAttribute(name= SignatureConstants.MESSAGE /* message */)
    private String message;

    /**
     * @zm-api-field-tag certificate
     * @zm-api-field-description MailCertificate certificate
     */
    @XmlElement(name= SignatureConstants.CERTIFICATE /* certificate */)
    private MailCertificate certificate;

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public MailCertificate getCertificate() {
        return certificate;
    }

    public MailSignature() {}
    public MailSignature(boolean valid, String message, MailCertificate certificate) {
        this.valid = valid;
        this.message = message;
        this.certificate = certificate;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCertificate(MailCertificate certificate) {
        this.certificate = certificate;
    }
}
