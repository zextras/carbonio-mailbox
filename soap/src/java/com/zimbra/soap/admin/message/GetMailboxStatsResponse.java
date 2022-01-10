// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxStats;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_MAILBOX_STATS_RESPONSE)
@XmlType(propOrder = {})
public class GetMailboxStatsResponse {

    /**
     * @zm-api-field-description Statistics about mailboxes
     */
    @XmlElement(name=AdminConstants.E_STATS, required=true)
    private MailboxStats stats;

    public GetMailboxStatsResponse() {
    }

    public void setStats(MailboxStats stats) {
        this.stats = stats;
    }

    public MailboxStats getStats() { return stats; }
}
