// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.soap.admin.message.GetAccountInfoResponse;
import com.zimbra.soap.admin.type.Attr;


public class SoapAccountInfo {
    
    private Map<String,Object> mAttrs;
    private String mName;
    private List<String> mSoapURL;
    private String mAdminSoapURL;
    
    SoapAccountInfo(GetAccountInfoResponse resp)
    throws ServiceException {
        mAttrs = Attr.collectionToMap(resp.getAttrList());
        mName = resp.getName();
        mSoapURL = resp.getSoapURLList();
        mAdminSoapURL = resp.getAdminSoapURL();
    }

	public String getAdminSoapURL() { return mAdminSoapURL; }
    
    public String getAttr(String name) {
        Object v = mAttrs.get(name);
        if (v instanceof String) {
            return (String) v;
        } else if (v instanceof String[]) {
            String[] a = (String[]) v;
            return a.length > 0 ? a[0] : null;
        } else {
            return null;
        }
    }

}