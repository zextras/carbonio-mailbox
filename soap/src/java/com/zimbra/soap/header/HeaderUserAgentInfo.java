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
public class HeaderUserAgentInfo {

    /**
     * @zm-api-field-tag user-agent-name
     * @zm-api-field-description User agent name
     */
    @XmlAttribute(name=HeaderConstants.A_NAME /* name */, required=false)
    private String name;

    /**
     * @zm-api-field-tag user-agent-version
     * @zm-api-field-description User agent version
     */
    @XmlAttribute(name=HeaderConstants.A_VERSION /* version */, required=false)
    private String version;

    public HeaderUserAgentInfo() {
    }

    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public String getName() { return name; }
    public String getVersion() { return version; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("version", version);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
