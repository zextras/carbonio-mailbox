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
public class MailQueueWithAction {

    /**
     * @zm-api-field-tag queue-name
     * @zm-api-field-description Queue name
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-description Action
     */
    @XmlElement(name=AdminConstants.E_ACTION /* action */, required=true)
    private final MailQueueAction action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailQueueWithAction() {
        this((String) null, (MailQueueAction) null);
    }

    public MailQueueWithAction(String name, MailQueueAction action) {
        this.name = name;
        this.action = action;
    }

    public String getName() { return name; }
    public MailQueueAction getAction() { return action; }
}
