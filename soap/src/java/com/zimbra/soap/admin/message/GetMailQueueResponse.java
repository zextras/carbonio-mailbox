// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerMailQueueDetails;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_MAIL_QUEUE_RESPONSE)
public class GetMailQueueResponse {

    /**
     * @zm-api-field-description Server Mail Queue details
     */
    @XmlElement(name=AdminConstants.E_SERVER, required=true)
    private final ServerMailQueueDetails server;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetMailQueueResponse() {
        this((ServerMailQueueDetails) null);
    }

    public GetMailQueueResponse(ServerMailQueueDetails server) {
        this.server = server;
    }

    public ServerMailQueueDetails getServer() { return server; }
}
