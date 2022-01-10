// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_ACTIVE_SERVERS_RESPONSE)
public class GetAllActiveServersResponse {

    /**
     * @zm-api-field-description Information about active servers
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private final List <ServerInfo> serverList = new ArrayList<ServerInfo>();

    public GetAllActiveServersResponse() {
    }

    public void addServer(ServerInfo server ) {
        this.getServerList().add(server);
    }

    public List <ServerInfo> getServerList() { return serverList; }
}
