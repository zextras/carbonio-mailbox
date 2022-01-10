// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.BackupConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class PurgeMovedMailboxInfo {

    /**
     * @zm-api-field-tag server-hostname
     * @zm-api-field-description Hostname of server the purge took place on
     */
    @XmlAttribute(name=BackupConstants.A_SERVER /* server */, required=true)
    private final String server;

    /**
     * @zm-api-field-tag purged-mailbox-id
     * @zm-api-field-description Purged mailbox ID
     */
    @XmlAttribute(name=BackupConstants.A_MAILBOXID /* mbxid */, required=true)
    private final int mailboxId;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private PurgeMovedMailboxInfo() {
        this((String) null, -1);
    }

    public PurgeMovedMailboxInfo(String server, int mailboxId) {
        this.server = server;
        this.mailboxId = mailboxId;
    }

    public String getServer() { return server; }
    public int getMailboxId() { return mailboxId; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("server", server)
            .add("mailboxId", mailboxId);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
