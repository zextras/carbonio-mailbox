// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxByAccountIdSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Recalculate Mailbox counts.
 * <br />
 * Forces immediate recalculation of total mailbox quota usage and all folder unread and size counts
 * <br />
 * <b>Access</b>: domain admin sufficient
 * <br />
 * note: this request is by default proxied to the account's home server
 */
@XmlRootElement(name=AdminConstants.E_RECALCULATE_MAILBOX_COUNTS_REQUEST)
public class RecalculateMailboxCountsRequest {

    /**
     * @zm-api-field-description Mailbox selector
     */
    @XmlElement(name=AdminConstants.E_MAILBOX, required=true)
    private final MailboxByAccountIdSelector mbox;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RecalculateMailboxCountsRequest() {
        mbox = null;
    }

    public RecalculateMailboxCountsRequest(MailboxByAccountIdSelector mbox) {
        this.mbox = mbox;
    }

    public MailboxByAccountIdSelector getMbox() {
        return mbox;
    }

}
