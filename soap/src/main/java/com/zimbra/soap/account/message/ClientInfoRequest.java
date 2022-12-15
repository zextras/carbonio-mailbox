// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_CLIENT_INFO_REQUEST)
public class ClientInfoRequest {

    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN /* domain */, required=true)
    private DomainSelector domain;

    public ClientInfoRequest() {
        this(null);
    }
    public ClientInfoRequest(DomainSelector domain) {
        this.domain = domain;
    }

    /**
     * @return the domain
     */
    public DomainSelector getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(DomainSelector domain) {
        this.domain = domain;
    }
}
