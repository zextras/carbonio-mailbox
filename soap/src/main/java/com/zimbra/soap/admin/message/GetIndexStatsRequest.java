// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxByAccountIdSelector;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_INDEX_STATS_REQUEST)
public class GetIndexStatsRequest {

    /**
     * @zm-api-field-description Mailbox
     */
    @XmlElement(name=AdminConstants.E_MAILBOX /* mbox */, required=true)
    private final MailboxByAccountIdSelector mbox;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetIndexStatsRequest() {
        this(null);
    }

    public GetIndexStatsRequest(MailboxByAccountIdSelector mbox) {
        this.mbox = mbox;
    }

    public MailboxByAccountIdSelector getMbox() { return mbox; }

}
