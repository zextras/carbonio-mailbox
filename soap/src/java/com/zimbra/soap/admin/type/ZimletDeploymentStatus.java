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
public class ZimletDeploymentStatus {

    /**
     * @zm-api-field-tag server-name
     * @zm-api-field-description Server name
     */
    @XmlAttribute(name=AdminConstants.A_SERVER /* server */, required=true)
    private final String server;

    /**
     * @zm-api-field-tag status-succeeded|failed|pending
     * @zm-api-field-description Status - valid values <b>succeeded|failed|pending</b>
     */
    @XmlAttribute(name=AdminConstants.A_STATUS /* status */, required=true)
    private final String status;

    /**
     * @zm-api-field-tag error-message
     * @zm-api-field-description Error message
     */
    @XmlAttribute(name=AdminConstants.A_ERROR /* error */, required=false)
    private final String error;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ZimletDeploymentStatus() {
        this((String) null, (String) null, (String) null);
    }

    public ZimletDeploymentStatus(String server, String status, String error) {
        this.server = server;
        this.status = status;
        this.error = error;
    }

    public String getServer() { return server; }
    public String getStatus() { return status; }
    public String getError() { return error; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("server", server)
            .add("status", status)
            .add("error", error);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
