// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_DOMAIN_RESPONSE)
@XmlType(propOrder = {AdminConstants.E_DOMAIN})
public class CreateDomainResponse {

    /**
     * @zm-api-field-description Information about the newly created domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN) private DomainInfo domain;
    public CreateDomainResponse() {
    }

    public void setDomain(DomainInfo domain) {
        this.domain = domain;
    }

    public DomainInfo getDomain() {
        return domain;
    }

}
