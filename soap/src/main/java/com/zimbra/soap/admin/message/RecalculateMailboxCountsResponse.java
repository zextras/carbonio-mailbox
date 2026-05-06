// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxQuotaInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_RECALCULATE_MAILBOX_COUNTS_RESPONSE)
@XmlType(propOrder = {})
public class RecalculateMailboxCountsResponse {

    /**
     * @zm-api-field-description Information about mailbox quotas
     */
    @XmlElement(name=AdminConstants.E_MAILBOX, required=true)
    private MailboxQuotaInfo mailbox;

    public RecalculateMailboxCountsResponse() {
    }

    public void setMailbox(MailboxQuotaInfo mailbox) {
        this.mailbox = mailbox;
    }

    public MailboxQuotaInfo getMailbox() { return mailbox; }
}
