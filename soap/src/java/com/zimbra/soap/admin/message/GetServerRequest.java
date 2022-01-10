// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Server
 */
@XmlRootElement(name=AdminConstants.E_GET_SERVER_REQUEST)
public class GetServerRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-tag apply-config
     * @zm-api-field-description If <b>{apply-config}</b> is <b>1 (true)</b>, then certain unset attrs on a server
     * will get their values from the global config.
     * <br />
     * if <b>{apply-config}</b> is <b>0 (false)</b>, then only attributes directly set on the server will be returned
     */
    @XmlAttribute(name=AdminConstants.A_APPLY_CONFIG, required=false)
    private ZmBoolean applyConfig;

    /**
     * @zm-api-field-description Server
     */
    @XmlElement(name=AdminConstants.E_SERVER)
    private ServerSelector server;

    public GetServerRequest() {
        this(null, null);
    }

    public GetServerRequest(ServerSelector server, Boolean applyConfig) {
        setServer(server);
        setApplyConfig(applyConfig);
    }

    public void setServer(ServerSelector server) {
        this.server = server;
    }

    public void setApplyConfig(Boolean applyConfig) {
        this.applyConfig = ZmBoolean.fromBool(applyConfig);
    }

    public ServerSelector getServer() { return server; }
    public Boolean isApplyConfig() { return ZmBoolean.toBool(applyConfig); }
}
