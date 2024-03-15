// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ServerQueues {

    /**
     * @zm-api-field-tag mta-server
     * @zm-api-field-description MTA server
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=true)
    private final String serverName;

    /**
     * @zm-api-field-description Queue information
     */
    @XmlElement(name=AdminConstants.E_QUEUE /* queue */, required=false)
    private List<MailQueueCount> queues = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ServerQueues() {
        this(null);
    }

    public ServerQueues(String serverName) {
        this.serverName = serverName;
    }

    public void setQueues(Iterable <MailQueueCount> queues) {
        this.queues.clear();
        if (queues != null) {
            Iterables.addAll(this.queues,queues);
        }
    }

    public ServerQueues addQueue(MailQueueCount queue) {
        this.queues.add(queue);
        return this;
    }

    public String getServerName() { return serverName; }
    public List<MailQueueCount> getQueues() {
        return Collections.unmodifiableList(queues);
    }
}
