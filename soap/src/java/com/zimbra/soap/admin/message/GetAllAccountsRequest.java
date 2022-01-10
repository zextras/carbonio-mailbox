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
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get All accounts matching the selectin criteria
 * <br />
 * <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_ACCOUNTS_REQUEST)
@XmlType(propOrder = {})
public class GetAllAccountsRequest {

    /**
     * @zm-api-field-description Server
     */
    @XmlElement(name=AdminConstants.E_SERVER, required=false)
    private ServerSelector server;

    /**
     * @zm-api-field-description Domain
     */
    @XmlElement(name=AdminConstants.E_DOMAIN, required=false)
    private DomainSelector domain;

    public GetAllAccountsRequest() {
        this(null, null);
    }

    public GetAllAccountsRequest(ServerSelector server, DomainSelector domain) {
        setServer(server);
        setDomain(domain);
    }

    public void setServer(ServerSelector server) {
        this.server = server;
    }

    public void setDomain(DomainSelector domain) {
        this.domain = domain;
    }

    public ServerSelector getServer() { return server; }
    public DomainSelector getDomain() { return domain; }
}
