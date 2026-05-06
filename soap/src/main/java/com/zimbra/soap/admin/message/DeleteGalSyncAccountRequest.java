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
import com.zimbra.soap.type.AccountSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Delete a Global Address List (GAL) Synchronisation account
 * <br />
 * Remove its zimbraGalAccountId from the domain, then deletes the account.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DELETE_GAL_SYNC_ACCOUNT_REQUEST)
public class DeleteGalSyncAccountRequest {

    /**
     * @zm-api-field-description Account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=true)
    private final AccountSelector account;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeleteGalSyncAccountRequest() {
        this(null);
    }

    public DeleteGalSyncAccountRequest(AccountSelector account) {
        this.account = account;
    }

    public AccountSelector getAccount() { return account; }
}
