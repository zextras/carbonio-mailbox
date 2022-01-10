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
