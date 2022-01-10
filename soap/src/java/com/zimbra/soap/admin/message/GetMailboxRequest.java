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
import com.zimbra.soap.admin.type.MailboxByAccountIdSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get a Mailbox
 * <br />
 * <b>Access</b>: domain admin sufficient
 * <br />
 * <br />
 * note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_MAILBOX_REQUEST)
@XmlType(propOrder = {})
public class GetMailboxRequest {

    /**
     * @zm-api-field-description Mailbox selector
     */
    @XmlElement(name=AdminConstants.E_MAILBOX, required=false)
    private final MailboxByAccountIdSelector mbox;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetMailboxRequest() {
        this(null);
    }

    public GetMailboxRequest(MailboxByAccountIdSelector mbox) {
        this.mbox = mbox;
    }

    public MailboxByAccountIdSelector getMbox() { return mbox; }
}
