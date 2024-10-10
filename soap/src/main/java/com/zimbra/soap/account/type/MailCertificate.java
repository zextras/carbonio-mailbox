package com.zimbra.soap.account.type;

import com.zimbra.common.soap.SignatureConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class MailCertificate {

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
     * @zm-api-field-description MailCertificateIssuer issuer
     */
    @XmlElement(name= SignatureConstants.ISSUER /* issuer*/, required=true)
    private MailCertificateIssuer issuer;

    public MailCertificate() {}
    public MailCertificate(String email, Long notBefore, Long notAfter, MailCertificateIssuer issuer) {
        this.email = email;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.issuer = issuer;
    }

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

    public MailCertificateIssuer getIssuer() {
        return issuer;
    }

    public void setIssuer(MailCertificateIssuer issuer) {
        this.issuer = issuer;
    }
}
