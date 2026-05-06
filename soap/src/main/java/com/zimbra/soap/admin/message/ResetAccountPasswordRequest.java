// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AccountSelector;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_RESET_ACCOUNT_PASSWORD_REQUEST)
public class ResetAccountPasswordRequest {
    /**
     * @zm-api-field-description Account
     */
    @XmlElement(name=AccountConstants.E_ACCOUNT, required=true)
    private AccountSelector account;

    /**
     * no-argument constructor wanted by JAXB
     */
    public ResetAccountPasswordRequest() {
        this(null);
    }

    public ResetAccountPasswordRequest(AccountSelector account) {
        this.account = account;
    }
}
