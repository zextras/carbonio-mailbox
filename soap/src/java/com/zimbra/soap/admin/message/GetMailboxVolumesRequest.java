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
import com.zimbra.soap.admin.type.Name;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns the info on blob and index volumes of a mailbox.  Only the volumes that have
 * data for the mailbox are returned.  The rootpath attribute is the root of the mailbox data, rather than the root
 * of the volume.  Also returns the current sync token of the mailbox.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_GET_MAILBOX_VOLUMES_REQUEST)
public class GetMailboxVolumesRequest {

    /**
     * @zm-api-field-tag account-email-address
     * @zm-api-field-description Account email address
     */
    @XmlElement(name=BackupConstants.E_ACCOUNT /* account */, required=true)
    private Name account;

    private GetMailboxVolumesRequest() {
    }

    private GetMailboxVolumesRequest(Name account) {
        setAccount(account);
    }

    public static GetMailboxVolumesRequest create(Name account) {
        return new GetMailboxVolumesRequest(account);
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
