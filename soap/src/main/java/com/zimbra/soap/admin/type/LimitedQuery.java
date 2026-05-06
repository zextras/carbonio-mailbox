// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class LimitedQuery {

    /**
     * @zm-api-field-tag query
     * @zm-api-field-description Query
     */
    @XmlValue private final String text;
    /**
     * @zm-api-field-tag query-limit
     * @zm-api-field-description Limit.  Default value 10
     */
    @XmlAttribute(name=AdminConstants.A_LIMIT /* limit */, required=false)
    private final Long limit;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private LimitedQuery() {
        this(null, null);
    }

    public LimitedQuery(String text, Long limit) {
        this.text = text;
        this.limit = limit;
    }

    public String getText() { return text; }
    public Long getLimit() { return limit; }
}
