// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerInfo;

@XmlRootElement(name=AdminConstants.E_GET_SERVER_RESPONSE)
public class GetServerResponse {

    /**
     * @zm-api-field-description Information about server
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private final ServerInfo server;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetServerResponse() {
        this(null);
    }

    public GetServerResponse(ServerInfo server) {
        this.server = server;
    }

    public ServerInfo getServer() { return server; }
}
