// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.GranteeType;

@XmlAccessorType(XmlAccessType.NONE)
public class GranteeInfo {

    /**
     * @zm-api-field-tag grantee-type
     * @zm-api-field-description Grantee type
     */
    @XmlAttribute(name=AdminConstants.A_TYPE, required=false)
    private final GranteeType type;

    /**
     * @zm-api-field-tag grantee-id
     * @zm-api-field-description Grantee ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag grantee-name
     * @zm-api-field-description Grantee name
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String name;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GranteeInfo() {
        this(null, null, null);
    }

    public GranteeInfo(GranteeType type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }
    public GranteeType getType() { return type; }
    public String getId() { return id; }
    public String getName() { return name; }
}
