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
import com.zimbra.cs.fb.FreeBusyProvider;
import com.zimbra.cs.fb.FreeBusyProvider.FreeBusySyncQueue;
import com.zimbra.soap.ZimbraSoapContext;

public class GetFreeBusyQueueInfo extends AdminDocumentHandler {
	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        
        // allow only system admin for now
        checkRight(zsc, context, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);
        
        String name = null;
        Element provider = request.getOptionalElement(AdminConstants.E_PROVIDER);
        if (provider != null)
        	name = provider.getAttribute(AdminConstants.A_NAME);
        
        Element response = zsc.createElement(AdminConstants.GET_FREE_BUSY_QUEUE_INFO_RESPONSE);
        if (name != null) {
        	FreeBusyProvider prov = FreeBusyProvider.getProvider(name);
        	if (prov == null)
        		throw ServiceException.INVALID_REQUEST("provider not found: "+name, null);
        	handleProvider(response, prov);
        } else {
        	for (FreeBusyProvider prov : FreeBusyProvider.getProviders()) {
        		handleProvider(response, prov);
        	}
        }
	    return response;
	}
	private void handleProvider(Element response, FreeBusyProvider prov) {
        Element provider = response.addElement(AdminConstants.E_PROVIDER);
        provider.addAttribute(AdminConstants.A_NAME, prov.getName());
        FreeBusySyncQueue queue = prov.getSyncQueue();
        if (queue == null)
        	return;
        synchronized (queue) {
        	for (String id : queue)
        		provider.addElement(AdminConstants.E_ACCOUNT).addAttribute(AdminConstants.A_ID, id);
        }
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
    }
}
