// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.DiscoverRightsInfo;

@XmlRootElement(name=AccountConstants.E_DISCOVER_RIGHTS_RESPONSE)
public class DiscoverRightsResponse {

    /**
     * @zm-api-field-description Information about targets for rights
     */
    @XmlElement(name=AccountConstants.E_TARGETS, required=false)
    private List<DiscoverRightsInfo> discoveredRights = Lists.newArrayList();

    public DiscoverRightsResponse() {
        this(null);
    }

    public DiscoverRightsResponse(Iterable<DiscoverRightsInfo> targets) {
        if (targets != null) {
            setDiscoveredRights(targets);
        }
    }

    public void setDiscoveredRights(Iterable<DiscoverRightsInfo> discoveredRights) {
        this.discoveredRights = Lists.newArrayList(discoveredRights);
    }

    public void addDiscoveredRight(DiscoverRightsInfo discoveredRight) {
        this.discoveredRights.add(discoveredRight);
    }

    public List<DiscoverRightsInfo> getDiscoveredRights() {
        return discoveredRights;
    }
}
