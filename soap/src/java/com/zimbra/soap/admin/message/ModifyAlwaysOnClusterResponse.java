// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AlwaysOnClusterInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_ALWAYSONCLUSTER_RESPONSE)
@XmlType(propOrder = {})
public class ModifyAlwaysOnClusterResponse {

    /**
     * @zm-api-field-description Information about server
     */
    @XmlElement(name=AdminConstants.E_ALWAYSONCLUSTER)
    private AlwaysOnClusterInfo cluster;

    public ModifyAlwaysOnClusterResponse() {
    }

    public void setAlwaysOnCluster(AlwaysOnClusterInfo cluster) {
        this.cluster = cluster;
    }

    public AlwaysOnClusterInfo getAlwaysOnCluster() {
        return cluster;
    }
}
