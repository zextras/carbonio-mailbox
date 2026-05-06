// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import com.zimbra.soap.admin.type.AccountInfo;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ACCOUNT_RESPONSE)
@XmlType(propOrder = {AdminConstants.E_ACCOUNT})
public class GetAccountResponse {

    /**
     * @zm-api-field-description Account information
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT /* account */, required=true)
    private AccountInfo account;

    public GetAccountResponse() {
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public AccountInfo getAccount() {
        return account;
    }

}
