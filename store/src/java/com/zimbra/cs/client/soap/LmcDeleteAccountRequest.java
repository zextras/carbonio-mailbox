// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.AdminConstants;

public class LmcDeleteAccountRequest extends LmcSoapRequest {
    String mAccountId;

    public LmcDeleteAccountRequest(String accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId cannot be null");
        }
        mAccountId = accountId;
    }

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(AdminConstants.DELETE_ACCOUNT_REQUEST);
        DomUtil.add(request, AdminConstants.E_ID, mAccountId);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML) {
        return new LmcDeleteAccountResponse();
    }
}
