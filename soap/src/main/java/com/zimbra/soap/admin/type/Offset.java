// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class Offset {

    /**
     * @zm-api-field-tag offset
     * @zm-api-field-description Offset
     */
    @XmlAttribute(name=AdminConstants.A_OFFSET /* offset */, required=true)
    private final long offset;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private Offset() {
        this(-1L);
    }

    public Offset(long offset) {
        this.offset = offset;
    }

    public long getOffset() { return offset; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("offset", offset);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
