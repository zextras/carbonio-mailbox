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
import com.zimbra.soap.admin.type.VersionInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_VERSION_INFO_RESPONSE)
public class GetVersionInfoResponse {

    /**
     * @zm-api-field-description Version information
     */
    @XmlElement(name=AdminConstants.A_VERSION_INFO_INFO, required=true)
    private final VersionInfo info;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private GetVersionInfoResponse() {
        this((VersionInfo)null);
    }

    public GetVersionInfoResponse(VersionInfo info) {
        this.info = info;
    }
    public VersionInfo getInfo() { return info; }
}
