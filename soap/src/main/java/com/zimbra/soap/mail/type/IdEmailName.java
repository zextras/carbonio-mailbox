// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class IdEmailName {

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-tag email
     * @zm-api-field-description Email
     */
    @XmlAttribute(name=MailConstants.A_EMAIL /* email */, required=false)
    private String email;

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=false)
    private String name;

    public IdEmailName() {
    }

    public static IdEmailName fromId(String id) {
        IdEmailName obj = new IdEmailName();
        obj.setId(id);
        return obj;
    }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("email", email)
            .add("name", name);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
