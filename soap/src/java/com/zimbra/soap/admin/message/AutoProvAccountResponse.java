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
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_AUTO_PROV_ACCOUNT_RESPONSE)
@XmlType(propOrder = {})
public class AutoProvAccountResponse {

    /**
     * @zm-api-field-description Account information
     */
    @XmlElement(name=AccountConstants.E_ACCOUNT /* account */, required=true)
    private AccountInfo account;

    public AutoProvAccountResponse() {
    }

    public AutoProvAccountResponse(AccountInfo account) {
        setAccount(account);
    }

    public static AutoProvAccountResponse create(AccountInfo account) {
        return new AutoProvAccountResponse(account);
    }

    public void setAccount(AccountInfo account) { this.account = account; }
    public AccountInfo getAccount() { return account; }

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
