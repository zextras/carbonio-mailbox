// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.ClusterConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ClusterServerInfo {

    /**
     * @zm-api-field-tag cluster-server-name
     * @zm-api-field-description Cluster server name
     */
    @XmlAttribute(name=ClusterConstants.A_CLUSTER_SERVER_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag cluster-server-status
     * @zm-api-field-description Server status - 1 or 0
     */
    @XmlAttribute(name=ClusterConstants.A_CLUSTER_SERVER_STATUS /* status */, required=true)
    private final int status;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ClusterServerInfo() {
        this(null, -1);
    }

    public ClusterServerInfo(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public String getName() { return name; }
    public int getStatus() { return status; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("status", status);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
