// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.PurgeMovedMailboxInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_PURGE_MOVED_MAILBOX_RESPONSE)
@XmlType(propOrder = {})
public class PurgeMovedMailboxResponse {

    /**
     * @zm-api-field-description Information about purged mailbxo
     */
    @XmlElement(name=BackupConstants.E_MAILBOX /* mbox */, required=true)
    private final PurgeMovedMailboxInfo mailbox;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private PurgeMovedMailboxResponse() {
        this((PurgeMovedMailboxInfo) null);
    }

    public PurgeMovedMailboxResponse(PurgeMovedMailboxInfo mailbox) {
        this.mailbox = mailbox;
    }

    public PurgeMovedMailboxInfo getMailbox() { return mailbox; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("mailbox", mailbox);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
