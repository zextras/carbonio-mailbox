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
import com.zimbra.soap.type.NamedElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get a count of all the mail queues by counting the number of files in the queue
 * directories.  Note that the admin server waits for queue counting to complete before responding - client should
 * invoke requests for different servers in parallel.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_MAIL_QUEUE_INFO_REQUEST)
public class GetMailQueueInfoRequest {

    /**
     * @zm-api-field-tag mta-server
     * @zm-api-field-description MTA Server
     */
    @XmlElement(name=AdminConstants.E_SERVER, required=true)
    private final NamedElement server;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetMailQueueInfoRequest() {
        this(null);
    }

    public GetMailQueueInfoRequest(NamedElement server) {
        this.server = server;
    }

    public NamedElement getServer() { return server; }
}
