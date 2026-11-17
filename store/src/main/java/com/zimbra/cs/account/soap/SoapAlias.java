// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.AliasInfo;
import com.zimbra.soap.admin.type.Attr;

class SoapAlias extends Alias implements SoapEntry {

	SoapAlias(AliasInfo alias, Provisioning prov) throws ServiceException {
        super(alias.getName(), alias.getName(),
                Attr.collectionToMap(alias.getAttrList()), prov);
    }

	public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable) throws ServiceException {
        throw new UnsupportedOperationException();
    }

    public void reload(SoapProvisioning prov) throws ServiceException {

    }
}
