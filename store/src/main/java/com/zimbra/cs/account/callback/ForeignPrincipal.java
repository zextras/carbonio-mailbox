// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;


public class ForeignPrincipal extends AttributeCallback {
    
    @Override
    public void preModify(CallbackContext context, String attrName, Object value,
            Map attrsToModify, Entry entry) 
    throws ServiceException {
        
        if (entry == null || context.isCreate()) {
            return;
        }
        
        if (!(entry instanceof Account))
            return;
            
        Provisioning prov = Provisioning.getInstance();
        if (!(prov instanceof LdapProv))
            return;
        
        Account acct = (Account)entry;
        ((LdapProv) prov).removeFromCache(acct);
    }
    
    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }
    
}
