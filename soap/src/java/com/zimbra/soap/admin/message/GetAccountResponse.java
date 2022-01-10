// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import com.zimbra.soap.admin.type.AccountInfo;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
