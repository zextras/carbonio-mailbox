// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.AccountSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Information about an account
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_ACCOUNT_INFO_REQUEST)
public class GetAccountInfoRequest {

    /**
     * @zm-api-field-description Use to identify the account
     */
    @XmlElement(name=AccountConstants.E_ACCOUNT, required=true)
    private AccountSelector account;

    /**
     * no-argument constructor wanted by JAXB
     */
    public GetAccountInfoRequest() {}

    public void setAccount(AccountSelector account) {
        this.account = account;
    }

    public AccountSelector getAccount() { return account; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("account", account)
            .toString();
    }
}
