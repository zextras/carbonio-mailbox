package com.zimbra.soap.account.type;

import com.zimbra.common.soap.SignatureConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class MailCertificateIssuer {

    /**
     * @zm-api-field-tag trusted
     * @zm-api-field-description trusted
     */
    @XmlAttribute(name= SignatureConstants.TRUSTED /* trusted*/, required=true)
    private boolean trusted;

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description name
     */
    @XmlAttribute(name= SignatureConstants.NAME /* name*/, required=true)
    private String name;

    public MailCertificateIssuer() {}
    public MailCertificateIssuer(boolean trusted, String name) {
        this.trusted = trusted;
        this.name = name;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
