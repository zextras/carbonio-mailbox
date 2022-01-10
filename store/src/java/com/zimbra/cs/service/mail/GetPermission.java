// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.soap.ZimbraSoapContext;

/*
 * Delete this class in bug 66989
 */

public class GetPermission extends MailDocumentHandler {
    
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canAccessAccount(zsc, account))
            throw ServiceException.PERM_DENIED("can not access account");

        Set<Right> specificRights = null;
        for (Element eACE : request.listElements(MailConstants.E_ACE)) {
            if (specificRights == null)
                specificRights = new HashSet<Right>();
            specificRights.add(RightManager.getInstance().getUserRight(eACE.getAttribute(MailConstants.A_RIGHT)));
        }
        
        List<ZimbraACE> aces = (specificRights==null)?ACLUtil.getAllACEs(account) : ACLUtil.getACEs(account, specificRights);
        Element response = zsc.createElement(MailConstants.GET_PERMISSION_RESPONSE);
        if (aces != null) {
            for (ZimbraACE ace : aces)
                ToXML.encodeACE(response, ace);
        }
        return response;
    }
}


