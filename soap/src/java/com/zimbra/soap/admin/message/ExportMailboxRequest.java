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

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.ExportMailboxSelector;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Export Mailbox (OLD mailbox move mechanism)
 * <br />
 * This request blocks until mailbox move is complete and can take a long time.  Client side should set timeout
 * accordingly.
 * <br />
 * Note: This is the old mailbox move request.  The new mailbox move request is <b>MoveMailboxRequest</b>.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_EXPORTMAILBOX_REQUEST)
public class ExportMailboxRequest {

    /**
     * @zm-api-field-description Export Mailbox details
     */
    @ZimbraUniqueElement
    @XmlElement(name=BackupConstants.E_ACCOUNT /* account */, required=true)
    private final ExportMailboxSelector account;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ExportMailboxRequest() {
        this((ExportMailboxSelector) null);
    }

    public ExportMailboxRequest(ExportMailboxSelector account) {
        this.account = account;
    }

    public ExportMailboxSelector getAccount() { return account; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("account", account);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
