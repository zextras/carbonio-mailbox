// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerWithQueueAction;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Command to act on invidual queue files.  This proxies through to postsuper.
 * <br />
 * list-of-ids can be ALL.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MAIL_QUEUE_ACTION_REQUEST)
public class MailQueueActionRequest {

    /**
     * @zm-api-field-description Server with queue action
     */
    @XmlElement(name=AdminConstants.E_SERVER, required=true)
    private final ServerWithQueueAction server;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailQueueActionRequest() {
        this(null);
    }

    public MailQueueActionRequest(ServerWithQueueAction server) {
        this.server = server;
    }

    public ServerWithQueueAction getServer() { return server; }
}
