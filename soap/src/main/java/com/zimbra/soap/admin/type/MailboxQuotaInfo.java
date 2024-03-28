// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MAILBOX)
@XmlType(propOrder = {})
public class MailboxQuotaInfo {

    /**
     * @zm-api-field-tag account-id
     * @zm-api-field-description Account ID
     */
    @XmlAttribute(name=AdminConstants.A_ACCOUNTID /* id */, required=true)
    private final String accountId;

    /**
     * @zm-api-field-tag quota-used
     * @zm-api-field-description Quota used
     */
    @XmlAttribute(name=AdminConstants.A_QUOTA_USED /* used */, required=true)
    private final long quotaUsed;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailboxQuotaInfo() {
        this(null, 0);
    }

    public MailboxQuotaInfo(String accountId, long quotaUsed) {
        this.accountId = accountId;
        this.quotaUsed = quotaUsed;
    }

    public String getAccountId() { return accountId; }
    public long getQuotaUsed() { return quotaUsed; }
}
