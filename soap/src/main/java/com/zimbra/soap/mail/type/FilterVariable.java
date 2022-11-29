// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
@JsonPropertyOrder({ "name", "value" })
public final class FilterVariable {

    /**
     * @zm-api-field-tag rule-name
     * @zm-api-field-description Rule name
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=true)
    private String name;

    /**
     * @zm-api-field-tag value
     * @zm-api-field-description Value
     */
    @XmlAttribute(name=MailConstants.A_VALUE /* value */, required=true)
    private String value;

    /**
     * no-argument constructor wanted by JAXB
     */
    public FilterVariable() {
        this(null, null);
    }

    public FilterVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static FilterVariable createFilterVariable(String name, String value) {
        return new FilterVariable(name, value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("value", value)
            .toString();
    }
}
