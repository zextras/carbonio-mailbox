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
public class TimeAttr {

    /**
     * @zm-api-field-tag ts
     * @zm-api-field-description ts
     */
    @XmlAttribute(name=AdminConstants.A_TIME /* time */, required=true)
    private final String time;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private TimeAttr() {
        this(null);
    }

    public TimeAttr(String time) {
        this.time = time;
    }

    public String getEndTime() { return time; }
}
