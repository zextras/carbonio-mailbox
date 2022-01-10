// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class NameOrId {

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=false)
    private String name;

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    public NameOrId() {
    }

    public static NameOrId createForName(String name) {
        NameOrId obj = new NameOrId();
        obj.setName(name);
        return obj;
    }

    public static NameOrId createForId(String id) {
        NameOrId obj = new NameOrId();
        obj.setId(id);
        return obj;
    }

    public static NameOrId createForNameAndId(String name, String id) {
        NameOrId obj = new NameOrId();
        obj.setName(name);
        obj.setId(id);
        return obj;
    }

    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getId() { return id; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("id", id);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
