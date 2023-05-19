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
public class FailoverClusterServiceSpec {

    /**
     * @zm-api-field-tag cluster-svc-name
     * @zm-api-field-description Cluster service name
     */
    @XmlAttribute(name=ClusterConstants.A_CLUSTER_SERVICE_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag new-server
     * @zm-api-field-description New Server
     */
    @XmlAttribute(name=ClusterConstants.A_FAILOVER_NEW_SERVER /* newServer */, required=true)
    private final String newServer;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FailoverClusterServiceSpec() {
        this(null, null);
    }

    public FailoverClusterServiceSpec(String name, String newServer) {
        this.name = name;
        this.newServer = newServer;
    }

    public String getName() { return name; }
    public String getNewServer() { return newServer; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("newServer", newServer);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
