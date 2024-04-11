// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;

import com.zimbra.soap.admin.type.ServerInfo;

@XmlRootElement(name=AdminConstants.E_GET_ALL_SERVERS_RESPONSE)
public class GetAllServersResponse {

    /**
     * @zm-api-field-description Information about servers
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private List <ServerInfo> serverList = new ArrayList<>();

    public GetAllServersResponse() {
    }

    public void addServer(ServerInfo server ) {
        this.getServerList().add(server);
    }

    public List <ServerInfo> getServerList() { return serverList; }
}
