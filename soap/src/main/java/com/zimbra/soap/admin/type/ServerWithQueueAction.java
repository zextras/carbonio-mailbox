// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ServerWithQueueAction {

    /**
     * @zm-api-field-tag mta-server
     * @zm-api-field-description MTA server
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-description Queue
     */
    @XmlElement(name=AdminConstants.E_QUEUE /* queue */, required=true)
    private final MailQueueWithAction queue;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ServerWithQueueAction() {
        this((String) null, (MailQueueWithAction) null);
    }

    public ServerWithQueueAction(String name, MailQueueWithAction queue) {
        this.name = name;
        this.queue = queue;
    }

    public String getName() { return name; }
    public MailQueueWithAction getQueue() { return queue; }
}
