// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LicenseExpirationInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_LICENSE_INFO_RESPONSE)
public class GetLicenseInfoResponse {

    /**
     * @zm-api-field-description License expiration informatioe
     */
    @XmlElement(name=AdminConstants.E_LICENSE_EXPIRATION, required=true)
    private final LicenseExpirationInfo expiration;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private GetLicenseInfoResponse() {
        this((LicenseExpirationInfo)null);
    }

    public GetLicenseInfoResponse(LicenseExpirationInfo expiration) {
        this.expiration = expiration;
    }

    public LicenseExpirationInfo getExpiration() { return expiration; }
}
