// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

@XmlAccessorType(XmlAccessType.NONE)
public class DataSourceSpecifier extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag data-source-type
     * @zm-api-field-description Data source type
     */
    @XmlAttribute(name=AccountConstants.A_TYPE, required=true)
    private final DataSourceType type;

    /**
     * @zm-api-field-tag data-source-name
     * @zm-api-field-description Data source name
     */
    @XmlAttribute(name=AccountConstants.A_NAME, required=true)
    private final String name;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DataSourceSpecifier() {
        this(null, null);
    }

    public DataSourceSpecifier(DataSourceType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DataSourceType getType() { return type; }
    public String getName() { return name; }
}
