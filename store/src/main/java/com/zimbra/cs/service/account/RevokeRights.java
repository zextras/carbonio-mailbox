// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.soap.ZimbraSoapContext;

public class RevokeRights extends AccountDocumentHandler {
    
    @Override
    public Element handle(Element request, Map<String, Object> context) 
    throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canAccessAccount(zsc, account)) {
            throw ServiceException.PERM_DENIED("can not access account");
        }
        
        Set<ZimbraACE> aces = new HashSet<ZimbraACE>();
        for (Element eACE : request.listElements(AccountConstants.E_ACE)) {
            ZimbraACE ace = GrantRights.handleACE(eACE, zsc, false);
            aces.add(ace);
        }

        // TODO, change to Provisioning.grantPermission?
        List<ZimbraACE> revoked = ACLUtil.revokeRight(Provisioning.getInstance(), account, aces);
        Element response = zsc.createElement(AccountConstants.REVOKE_RIGHTS_RESPONSE);
        if (aces != null) {
            for (ZimbraACE ace : revoked) {
                ToXML.encodeACE(response, ace);
            }
        }
        return response;
    }
    
}
