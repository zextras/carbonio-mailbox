// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_ACCOUNTS_RESPONSE)
@XmlType(propOrder = {})
public class GetAllAccountsResponse {

    /**
     * @zm-api-field-description Information on accounts
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=false)
    private List <AccountInfo> accountList = Lists.newArrayList();

    public GetAllAccountsResponse() {
    }

    public void setAccountList(Iterable <AccountInfo> accounts) {
        this.accountList.clear();
        if (accounts != null) {
            Iterables.addAll(this.accountList, accounts);
        }
    }

    public void addAccount(AccountInfo account ) {
        this.accountList.add(account);
    }

    public List <AccountInfo> getAccountList() {
        return Collections.unmodifiableList(accountList);
    }
}
