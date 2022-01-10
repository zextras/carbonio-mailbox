// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.IndexStats;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_INDEX_STATS_RESPONSE)
public class GetIndexStatsResponse {
    /**
     * @zm-api-field-description Statistics about mailboxes
     */
    @XmlElement(name=AdminConstants.E_STATS, required=true)
    private IndexStats stats;

    public GetIndexStatsResponse() {
    }

    public void setStats(IndexStats stats) {
        this.stats = stats;
    }

    public IndexStats getStats() { return stats; }

}
