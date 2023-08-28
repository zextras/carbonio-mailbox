// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.util.SkinUtil;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAvailableSkins extends DocumentHandler  {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canAccessAccount(zsc, account)) {
            throw ServiceException.PERM_DENIED("can not access account");
        }

        String[] availSkins = SkinUtil.getSkins(account);

        Element response = zsc.createElement(AccountConstants.GET_AVAILABLE_SKINS_RESPONSE);
        for (String skin : availSkins) {
            Element skinElem = response.addElement(AccountConstants.E_SKIN);
            skinElem.addAttribute(AccountConstants.A_NAME, skin);
        }
        return response;
    }
}
