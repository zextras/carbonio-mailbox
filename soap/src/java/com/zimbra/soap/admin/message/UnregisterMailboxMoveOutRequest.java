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
import com.zimbra.soap.admin.type.MailboxMoveSpec;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description This request is invoked by move destination server against move source server to
 * indicate the completion of mailbox move.  This request is also invoked to reset the state after a mailbox move that
 * died unexpectedly, such as when the destination server crashed.
 * <br />
 * <br />
 * NO_SUCH_MOVE_OUT fault is returned if there is no move-out in progress.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST)
public class UnregisterMailboxMoveOutRequest {

    /**
     * @zm-api-field-description Details of Mailbox Move
     */
    @XmlElement(name=BackupConstants.E_ACCOUNT /* account */, required=true)
    private MailboxMoveSpec account;

    private UnregisterMailboxMoveOutRequest() {
    }

    private UnregisterMailboxMoveOutRequest(MailboxMoveSpec account) {
        setAccount(account);
    }

    public static UnregisterMailboxMoveOutRequest create(MailboxMoveSpec account) {
        return new UnregisterMailboxMoveOutRequest(account);
    }

    public void setAccount(MailboxMoveSpec account) { this.account = account; }
    public MailboxMoveSpec getAccount() { return account; }

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
