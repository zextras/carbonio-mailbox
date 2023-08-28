// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.fb.ExchangeEWSFreeBusyProvider;
import com.zimbra.cs.fb.FreeBusyProvider;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAllFreeBusyProviders extends AdminDocumentHandler {
	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        
        // allow only system admin for now
        checkRight(zsc, context, null, Admin.R_getAllFreeBusyProviders);
        
        Element response = zsc.createElement(AdminConstants.GET_ALL_FREE_BUSY_PROVIDERS_RESPONSE);
        
        for (FreeBusyProvider prov : FreeBusyProvider.getProviders()) {
            if (!(prov instanceof ExchangeEWSFreeBusyProvider )) {
                Element provElem = response.addElement(AdminConstants.E_PROVIDER);
                provElem.addAttribute(AdminConstants.A_NAME, prov.getName());
                provElem.addAttribute(AdminConstants.A_PROPAGATE, prov.registerForMailboxChanges());
                provElem.addAttribute(AdminConstants.A_START, prov.cachedFreeBusyStartTime());
                provElem.addAttribute(AdminConstants.A_END, prov.cachedFreeBusyEndTime());
                provElem.addAttribute(AdminConstants.A_QUEUE, prov.getQueueFilename());
                provElem.addAttribute(AdminConstants.A_PREFIX, prov.foreignPrincipalPrefix());
            }
        }
	    return response;
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getAllFreeBusyProviders);
    }
}
