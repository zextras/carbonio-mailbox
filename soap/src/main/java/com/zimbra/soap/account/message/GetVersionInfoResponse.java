// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.VersionInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_VERSION_INFO_RESPONSE)
public class GetVersionInfoResponse {

    /**
     * @zm-api-field-description Version information
     */
    @XmlElement(name=AccountConstants.E_VERSION_INFO_INFO, required=true)
    private final VersionInfo versionInfo;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetVersionInfoResponse() {
        this((VersionInfo) null);
    }

    public GetVersionInfoResponse(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public VersionInfo getVersionInfo() { return versionInfo; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("versionInfo", versionInfo)
            .toString();
    }
}
