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
import com.zimbra.soap.admin.type.ServerInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_SERVER_RESPONSE)
@XmlType(propOrder = {})
public class ModifyServerResponse {

    /**
     * @zm-api-field-description Information about server
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private ServerInfo server;

    public ModifyServerResponse() {
    }

    public void setServer(ServerInfo server) {
        this.server = server;
    }

    public ServerInfo getServer() {
        return server;
    }
}
