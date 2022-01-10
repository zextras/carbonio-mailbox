// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_CHECK_LICENSE_RESPONSE)
public class CheckLicenseResponse {

    @XmlEnum
    public enum CheckLicenseStatus {
        @XmlEnumValue("ok") OK("ok"),
        @XmlEnumValue("no") NO("no"),
        @XmlEnumValue("inGracePeriod") IN_GRACE_PERIOD("inGracePeriod");
        private final String name;

        private CheckLicenseStatus(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * @zm-api-field-description Status of access to requested licensed feature.
     */
    @XmlAttribute(name=AccountConstants.A_STATUS /* status */, required=true)
    private final CheckLicenseStatus status;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CheckLicenseResponse() {
        this((CheckLicenseStatus) null);
    }

    public CheckLicenseResponse(CheckLicenseStatus status) {
        this.status = status;
    }

    public CheckLicenseStatus getStatus() { return status; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("status", status);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
