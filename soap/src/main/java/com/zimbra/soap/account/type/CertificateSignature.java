// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CertificateSignature {

    /**
     * @zm-api-field-tag serialNumber
     * @zm-api-field-description serialNumber of the certificate, which is used to uniquely identify the certificate.
     */
    @ZimbraJsonAttribute
    @XmlElement(name=SmimeConstants.E_SERIAL_NO, required=false)
    private String serialNumber;

    /**
     * @zm-api-field-tag algorithm
     * @zm-api-field-description algorithm used to create the signature.
     */
    @ZimbraJsonAttribute
    @XmlElement(name=SmimeConstants.E_ALGORITHM, required=false)
    private String algorithm;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper.add("serialNo", serialNumber)
            .add("algorithm", algorithm);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

}
