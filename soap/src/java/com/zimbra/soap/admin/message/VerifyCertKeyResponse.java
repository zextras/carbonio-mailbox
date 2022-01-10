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
@XmlRootElement(name=CertMgrConstants.E_VERIFY_CERTKEY_RESPONSE)
public class VerifyCertKeyResponse {

    /**
     * @zm-api-field-tag verify-result
     * @zm-api-field-description Verify result - <b>true|false|invalid</b>
     */
    @XmlAttribute(name=CertMgrConstants.A_verifyResult /* verifyResult */, required=true)
    private String verifyResult;

    public VerifyCertKeyResponse() {
    }

    private VerifyCertKeyResponse(String verifyResult) {
        setVerifyResult(verifyResult);
    }

    public static VerifyCertKeyResponse createForVerifyResult(String verifyResult) {
        return new VerifyCertKeyResponse(verifyResult);
    }

    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getVerifyResult() { return verifyResult; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("verifyResult", verifyResult);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
