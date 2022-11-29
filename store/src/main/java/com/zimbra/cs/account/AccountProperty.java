// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;

public abstract class AccountProperty extends NamedEntry {
	private final Account mAcct; 
    
    AccountProperty(Account acct, String name, String id, Map<String, Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, null, prov);
        mAcct = acct;
    }
    
    public String getAccountId() {
        return mAcct.getId();
    }
    
    public Account getAccount() throws ServiceException {
        return mAcct;
    }
    
}
