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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=CertMgrConstants.E_UPLOAD_PROXYCA_RESPONSE)
public class UploadProxyCAResponse {

    /**
     * @zm-api-field-tag certificate-content
     * @zm-api-field-description Certificate content
     */
    @XmlAttribute(name=CertMgrConstants.A_cert_content /* cert_content */, required=false)
    private String certificateContent;

    private UploadProxyCAResponse() {
    }

    private UploadProxyCAResponse(String certificateContent) {
        setCertificateContent(certificateContent);
    }

    public static UploadProxyCAResponse createForCert(String certificateContent) {
        return new UploadProxyCAResponse(certificateContent);
    }

    public void setCertificateContent(String certificateContent) { this.certificateContent = certificateContent; }
    public String getCertificateContent() { return certificateContent; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("certificateContent", certificateContent);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
