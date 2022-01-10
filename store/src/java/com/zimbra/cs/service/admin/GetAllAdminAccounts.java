// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class GetAllAdminAccounts extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        boolean applyCos = request.getAttributeBool(AdminConstants.A_APPLY_COS, true);
        List accounts = prov.getAllAdminAccounts();
        
        AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);

        Element response = zsc.createElement(AdminConstants.GET_ALL_ADMIN_ACCOUNTS_RESPONSE);
        for (Iterator it=accounts.iterator(); it.hasNext(); ) {
            Account acct = (Account)it.next();
            
            if (aac.hasRightsToList(acct, Admin.R_listAccount, null))
                ToXML.encodeAccount(response, acct, applyCos, null, aac.getAttrRightChecker(acct));
        }
	    return response;
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listAccount);
        relatedRights.add(Admin.R_getAccount);
        
        notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
    }
}
