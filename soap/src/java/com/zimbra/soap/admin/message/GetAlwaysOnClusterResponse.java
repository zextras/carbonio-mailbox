// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AlwaysOnClusterInfo;

@XmlRootElement(name=AdminConstants.E_GET_ALWAYSONCLUSTER_RESPONSE)
public class GetAlwaysOnClusterResponse {

    /**
     * @zm-api-field-description Information about server
     */
    @XmlElement(name=AdminConstants.E_ALWAYSONCLUSTER)
    private final AlwaysOnClusterInfo cluster;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetAlwaysOnClusterResponse() {
        this(null);
    }

    public GetAlwaysOnClusterResponse(AlwaysOnClusterInfo cluster) {
        this.cluster = cluster;
    }

    public AlwaysOnClusterInfo getAlwaysOnCluster() { return cluster; }
}

