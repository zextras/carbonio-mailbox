// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class AccountsAttrib {

    /**
     * @zm-api-field-tag comma-sep-account-id-list
     * @zm-api-field-description Comma separated list of account IDs
     */
    @XmlAttribute(name=AdminConstants.A_ACCOUNTS /* accounts */, required=true)
    private final String accounts;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AccountsAttrib() {
        this(null);
    }

    public AccountsAttrib(String accounts) {
        this.accounts = accounts;
    }

    public String getAccounts() { return accounts; }
}
