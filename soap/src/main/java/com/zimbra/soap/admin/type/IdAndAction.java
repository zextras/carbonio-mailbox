// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class IdAndAction {

    /**
     * @zm-api-field-tag id-of-account
     * @zm-api-field-description Zimbra ID of account
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag bug72174|wiki|contactGroup
     * @zm-api-field-description bug72174 or wiki or contactGroup
     */
    @XmlAttribute(name=AdminConstants.A_ACTION, required=true)
    private final String action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private IdAndAction() {
        this(null, null);
    }

    public IdAndAction(String id, String action) {
        this.id = id;
        this.action = action;
    }

    public String getId() { return id; }
    public String getAction() { return action; }
}
