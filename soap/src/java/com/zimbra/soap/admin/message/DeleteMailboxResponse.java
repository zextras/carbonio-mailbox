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
import com.zimbra.soap.admin.type.MailboxWithMailboxId;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DELETE_MAILBOX_RESPONSE)
@XmlType(propOrder = {})
public class DeleteMailboxResponse {

    /**
     * @zm-api-field-description Details of the deleted mailbox.
     * <br />
     * Tthe <b>&lt;mbox></b> element is left out of the response if no mailbox existed for that account
     */
    @XmlElement(name=AdminConstants.E_MAILBOX, required=false)
    private MailboxWithMailboxId mbox;

    public DeleteMailboxResponse() {
    }

    public void setMbox(MailboxWithMailboxId mbox) {
        this.mbox = mbox;
    }

    public MailboxWithMailboxId getMbox() { return mbox; }
}
