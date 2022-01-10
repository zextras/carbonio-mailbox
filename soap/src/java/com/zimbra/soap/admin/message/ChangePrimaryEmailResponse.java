// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHANGE_PRIMARY_EMAIL_RESPONSE)
@XmlType(propOrder = {})
public class ChangePrimaryEmailResponse {
    /**
     * @zm-api-field-description Information about account after rename
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT)
    private AccountInfo account;

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public AccountInfo getAccount() { return account; }
}
