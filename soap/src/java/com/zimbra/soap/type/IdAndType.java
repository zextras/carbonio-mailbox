// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class IdAndType {

    /**
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AdminConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-description Type
     */
    @XmlAttribute(name=AdminConstants.A_TYPE /* type */, required=true)
    private final String type;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private IdAndType() {
        this((String) null, (String) null);
    }

    public IdAndType(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() { return id; }
    public String getType() { return type; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("type", type);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
