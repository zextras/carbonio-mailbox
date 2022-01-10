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
import com.zimbra.soap.type.NamedElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Command to invoke postqueue -f.  All queues cached in the server are stale after
 * invoking this because this is a global operation to all the queues in a given server.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MAIL_QUEUE_FLUSH_REQUEST)
public class MailQueueFlushRequest {

    /**
     * @zm-api-field-tag mta-server
     * @zm-api-field-description Mta server
     */
    @XmlElement(name=AdminConstants.E_SERVER, required=true)
    private final NamedElement server;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailQueueFlushRequest() {
        this((NamedElement) null);
    }

    public MailQueueFlushRequest(NamedElement server) {
        this.server = server;
    }

    public NamedElement getServer() { return server; }
}
