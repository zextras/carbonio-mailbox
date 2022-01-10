// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class UrlAndValue {

    /**
     * @zm-api-field-tag url
     * @zm-api-field-description URL
     */
    @XmlAttribute(name=MailConstants.A_URL /* url */, required=false)
    private String url;

    /**
     * @zm-api-field-tag value
     * @zm-api-field-description Value
     */
    @XmlValue
    private String value;

    public UrlAndValue() {
    }

    public void setUrl(String url) { this.url = url; }
    public void setValue(String value) { this.value = value; }
    public String getUrl() { return url; }
    public String getValue() { return value; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("url", url)
            .add("value", value);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
