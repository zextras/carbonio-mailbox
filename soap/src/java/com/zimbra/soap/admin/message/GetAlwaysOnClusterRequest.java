// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AlwaysOnClusterSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Server
 */
@XmlRootElement(name=AdminConstants.E_GET_ALWAYSONCLUSTER_REQUEST)
public class GetAlwaysOnClusterRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-description Server
     */
    @XmlElement(name=AdminConstants.E_ALWAYSONCLUSTER)
    private AlwaysOnClusterSelector cluster;

    public GetAlwaysOnClusterRequest() {
        this(null);
    }

    public GetAlwaysOnClusterRequest(AlwaysOnClusterSelector cluster) {
        setAlwaysOnCluster(cluster);
    }

    public void setAlwaysOnCluster(AlwaysOnClusterSelector cluster) {
        this.cluster = cluster;
    }

    public AlwaysOnClusterSelector getAlwaysOnCluster() { return cluster; }
}
