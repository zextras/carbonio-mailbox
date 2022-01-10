// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;

public interface AutoProvisionListener {
    public void postCreate(Domain domain, Account acct, String externalDN);
}
