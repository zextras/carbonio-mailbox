// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.ZimbraSoapContext;


public class GetSpellDictionaries
extends MailDocumentHandler {

    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext zc = getZimbraSoapContext(context);
        Server server = Provisioning.getInstance().getLocalServer();
        Element response = zc.createElement(MailConstants.GET_SPELL_DICTIONARIES_RESPONSE);

        for (String dictionary : server.getSpellAvailableDictionary()) {
            response.addElement(MailConstants.E_DICTIONARY).setText(dictionary);
        }
        
        return response;
    }
}
