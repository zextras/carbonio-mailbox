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

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_ACTIVATE_LICENSE_RESPONSE)
public class ActivateLicenseResponse {

    /**
     * @zm-api-field-description Server time in milliseconds
     */
    @XmlAttribute(name=AdminConstants.A_SERVER_TIME /* serverTime */, required=true)
    private final long serverTime;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ActivateLicenseResponse() {
        this(-1L);
    }

    public ActivateLicenseResponse(long serverTime) {
        this.serverTime = serverTime;
    }

    public long getServerTime() { return serverTime; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("serverTime", serverTime);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
