// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.header;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.HeaderConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HeaderFormatInfo {

    /**
     * @zm-api-field-tag response-format
     * @zm-api-field-description Desired response format.  Valid values "xml" (default) and "js"
     */
    @XmlAttribute(name=HeaderConstants.A_TYPE /* type */, required=false)
    private String format;

    public HeaderFormatInfo() {
    }

    public void setFormat(String format) { this.format = format; }
    public String getFormat() { return format; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("format", format);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
