// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.ClusterConstants;
import com.zimbra.soap.admin.type.FailoverClusterServiceSpec;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Failover Cluster Service
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=ClusterConstants.E_FAILOVER_CLUSTER_SERVICE_REQUEST)
public class FailoverClusterServiceRequest {

    /**
     * @zm-api-field-description Failover details
     */
    @XmlElement(name=ClusterConstants.A_CLUSTER_SERVICE /* service */, required=false)
    private FailoverClusterServiceSpec service;

    public FailoverClusterServiceRequest() {
    }

    public void setService(FailoverClusterServiceSpec service) {
        this.service = service;
    }
    public FailoverClusterServiceSpec getService() { return service; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("service", service);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
