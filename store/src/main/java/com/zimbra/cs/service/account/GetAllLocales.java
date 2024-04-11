// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.Locale;
import java.util.Map;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.util.WebClientL10nUtil;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAllLocales extends AccountDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        Locale[] locales = WebClientL10nUtil.getAllLocalesSorted();
        Element response = zsc.createElement(AccountConstants.GET_ALL_LOCALES_RESPONSE);
        for (Locale locale : locales) {
            ToXML.encodeLocale(response, locale, Locale.US);
        }
        return response;
    }
}
