// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class AttributeName {

    /**
     * @zm-api-field-tag attribute-name
     * @zm-api-field-description Attribute name
     */
    @XmlAttribute(name=MailConstants.A_ATTRIBUTE_NAME, required=true)
    private final String name;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected AttributeName() {
        this(null);
    }

    public AttributeName(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
