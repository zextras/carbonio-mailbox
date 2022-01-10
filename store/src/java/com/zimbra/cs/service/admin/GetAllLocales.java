// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.util.WebClientL10nUtil;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAllLocales extends AdminDocumentHandler {

    @Override
    public boolean domainAuthSufficient(Map<String, Object> context) {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        Locale locales[] = WebClientL10nUtil.getAllLocalesSorted();
        Element response = zsc.createElement(AdminConstants.GET_ALL_LOCALES_RESPONSE);
        for (Locale locale : locales) {
            com.zimbra.cs.service.account.ToXML.encodeLocale(response, locale, Locale.US);
        }
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add("Allow all admins");
    }
}
