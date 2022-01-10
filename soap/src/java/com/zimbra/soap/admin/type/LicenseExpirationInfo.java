// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class LicenseExpirationInfo {

    /**
     * @zm-api-field-tag expiration-date-YYYYMMDD
     * @zm-api-field-description Expiration date in format : <b>YYYYMMDD</b>
     */
    @XmlAttribute(name=AdminConstants.A_LICENSE_EXPIRATION_DATE /* date */, required=false)
    private final String date;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private LicenseExpirationInfo() {
        this((String)null);
    }

    public LicenseExpirationInfo(String date) {
        this.date = date;
    }

    public String getDate() { return date; }
}
