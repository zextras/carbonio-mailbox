// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Server
 */
@XmlRootElement(name=AdminConstants.E_SET_SERVER_OFFLINE_REQUEST)
public class SetServerOfflineRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-description Server
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private ServerSelector server;

    public SetServerOfflineRequest() {
        this(null);
    }

    public SetServerOfflineRequest(ServerSelector server) {
        setServer(server);
    }

    public void setServer(ServerSelector server) {
        this.server = server;
    }

    public ServerSelector getServer() { return server; }
}
