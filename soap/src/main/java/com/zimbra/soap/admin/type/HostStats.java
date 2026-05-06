// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HostStats {

    /**
     * @zm-api-field-tag hostname
     * @zm-api-field-description Hostname
     */
    @XmlAttribute(name=AdminConstants.A_HOSTNAME, required=true)
    private final String hostName;

    /**
     * @zm-api-field-description Stats information
     */
    @XmlElement(name=AdminConstants.E_STATS, required=false)
    private StatsInfo stats;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private HostStats() {
        this(null);
    }

    public HostStats(String hostName) {
        this.hostName = hostName;
    }

    public void setStats(StatsInfo stats) { this.stats = stats; }
    public String getHostName() { return hostName; }
    public StatsInfo getStats() { return stats; }
}
