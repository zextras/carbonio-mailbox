// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.SMIMEPublicCertsInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_SMIME_PUBLIC_CERTS_RESPONSE)
@XmlType(propOrder = {})
public class GetSMIMEPublicCertsResponse {

    /**
     * @zm-api-field-description SMIME public certificates
     */
    @XmlElement(name=AccountConstants.E_CERTS /* certs */, required=false)
    private List<SMIMEPublicCertsInfo> certs;

    public GetSMIMEPublicCertsResponse() {
    }

    public void setCerts(List<SMIMEPublicCertsInfo> certs) { this.certs = certs; }
    public List<SMIMEPublicCertsInfo> getCerts() { return certs; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("certs", certs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
