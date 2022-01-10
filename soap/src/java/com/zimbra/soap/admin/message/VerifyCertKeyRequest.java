// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.CertMgrConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Verify Certificate Key
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=CertMgrConstants.E_VERIFY_CERTKEY_REQUEST)
public class VerifyCertKeyRequest {

    /**
     * @zm-api-field-description Certificate
     */
    @XmlAttribute(name=CertMgrConstants.E_cert /* cert */, required=false)
    private String certificate;

    /**
     * @zm-api-field-description Private key
     */
    @XmlAttribute(name=CertMgrConstants.A_privkey /* privkey */, required=false)
    private String privateKey;

    public VerifyCertKeyRequest() {
    }

    private VerifyCertKeyRequest(String certificate, String privateKey) {
        setCertificate(certificate);
        setPrivateKey(privateKey);
    }

    public static VerifyCertKeyRequest createForCertAndPrivateKey(String certificate, String privateKey) {
        return new VerifyCertKeyRequest(certificate, privateKey);
    }

    public void setCertificate(String certificate) { this.certificate = certificate; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getCertificate() { return certificate; }
    public String getPrivateKey() { return privateKey; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("certificate", certificate)
            .add("privateKey", privateKey);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
