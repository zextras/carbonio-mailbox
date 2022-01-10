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
public class StatsInfo {

    /**
     * @zm-api-field-tag stat-name
     * @zm-api-field-description Stat name
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-description Stats values
     */
    @XmlElement(name=AdminConstants.E_VALUES /* values */, required=false)
    private final StatsValues values;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private StatsInfo() {
        this((String) null, (StatsValues) null);
    }

    public StatsInfo(String name, StatsValues values) {
        this.name = name;
        this.values = values;
    }

    public String getName() { return name; }
    public StatsValues getValues() { return values; }
}
