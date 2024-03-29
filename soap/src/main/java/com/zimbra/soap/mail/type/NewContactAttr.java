// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class NewContactAttr {

    /**
     * @zm-api-field-tag attr-name
     * @zm-api-field-description Attribute name
     */
    @XmlAttribute(name=MailConstants.A_ATTRIBUTE_NAME /* n */, required=true)
    private String name;

    /**
     * @zm-api-field-tag upload-id
     * @zm-api-field-description Upload ID
     */
    @XmlAttribute(name=MailConstants.A_ATTACHMENT_ID /* aid */, required=false)
    private String attachId;

    /**
     * @zm-api-field-tag item-id
     * @zm-api-field-description Item ID.  Used in combination with <b>subpart-name</b>
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private Integer id;

    /**
     * @zm-api-field-tag subpart-name
     * @zm-api-field-description Subpart Name
     */
    @XmlAttribute(name=MailConstants.A_PART /* part */, required=false)
    private String part;

    /**
     * @zm-api-field-tag attr-data
     * @zm-api-field-description Attribute data
     * <br />Date related attributes like "birthday" and "anniversary" SHOULD use <b>"yyyy-MM-dd"</b> format or,
     * if the year isn't specified <b>"--MM-dd"</b> format
     */
    @XmlValue
    private String value;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private NewContactAttr() {
         this(null);
    }

    public NewContactAttr( String name) {
         this.name = name;
    }

    public static NewContactAttr fromNameAndValue(String name, String value) {
        final NewContactAttr ncs = new NewContactAttr(name);
        ncs.setValue(value);
        return ncs;
    }

    public NewContactAttr setName(String name) { this.name = name; return this; }
    public NewContactAttr setAttachId(String attachId) { this.attachId = attachId; return this; }
    public NewContactAttr setId(Integer id) { this.id = id; return this; }
    public NewContactAttr setPart(String part) { this.part = part; return this; }
    public NewContactAttr setValue(String value) { this.value = value; return this; }

    public String getName() { return name; }
    public String getAttachId() { return attachId; }
    public Integer getId() { return id; }
    public String getPart() { return part; }
    public String getValue() { return value; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("attachId", attachId)
            .add("id", id)
            .add("part", part)
            .add("value", value);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
