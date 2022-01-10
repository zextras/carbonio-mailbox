// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
