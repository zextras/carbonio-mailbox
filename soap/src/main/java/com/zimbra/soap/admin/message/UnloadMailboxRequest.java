// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.Name;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Forces the mailbox of the specified account to get unloaded from memory.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_UNLOAD_MAILBOX_REQUEST)
public class UnloadMailboxRequest {

    /**
     * @zm-api-field-tag account-email-address
     * @zm-api-field-description Account email address
     */
    @XmlElement(name=BackupConstants.E_ACCOUNT /* account */, required=true)
    private Name account;

    private UnloadMailboxRequest() {
    }

    private UnloadMailboxRequest(Name account) {
        setAccount(account);
    }

    public static UnloadMailboxRequest create(Name account) {
        return new UnloadMailboxRequest(account);
    }

    public void setAccount(Name account) { this.account = account; }
    public Name getAccount() { return account; }

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
