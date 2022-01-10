// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AlwaysOnClusterInfo;

@XmlRootElement(name=AdminConstants.E_GET_ALL_ALWAYSONCLUSTERS_RESPONSE)
public class GetAllAlwaysOnClustersResponse {

    /**
     * @zm-api-field-description Information about alwaysOnClusters
     */
    @XmlElement(name=AdminConstants.E_ALWAYSONCLUSTER)
    private final List <AlwaysOnClusterInfo> clusterList = new ArrayList<AlwaysOnClusterInfo>();

    public GetAllAlwaysOnClustersResponse() {
    }

    public void addAlwaysOnCluster(AlwaysOnClusterInfo cluster ) {
        this.getAlwaysOnClusterList().add(cluster);
    }

    public List<AlwaysOnClusterInfo> getAlwaysOnClusterList() { return clusterList; }
}
