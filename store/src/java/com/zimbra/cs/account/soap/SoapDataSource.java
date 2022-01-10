// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.Map;

import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.soap.Element;

class SoapDataSource extends DataSource implements SoapEntry {
        
    SoapDataSource(Account acct, DataSourceType type, String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(acct, type, name, id, attrs, prov);
    }

    SoapDataSource(Account acct, Element e, Provisioning prov) throws ServiceException {
        super(acct,
              DataSourceType.fromString(e.getAttribute(AccountConstants.A_TYPE)),
              e.getAttribute(AccountConstants.A_NAME), e.getAttribute(AccountConstants.A_ID), SoapProvisioning.getAttrs(e), prov);
    }

    public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable) throws ServiceException {
        // not needed?        
    }

    public void reload(SoapProvisioning prov) throws ServiceException {
        // not needed?
    }

}
