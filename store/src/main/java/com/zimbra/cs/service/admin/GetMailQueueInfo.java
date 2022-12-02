// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
import com.zimbra.cs.rmgmt.RemoteResultParser;
import com.zimbra.soap.ZimbraSoapContext;

public class GetMailQueueInfo extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		ZimbraSoapContext zsc = getZimbraSoapContext(context);
	    Provisioning prov = Provisioning.getInstance();
	    
	    Element serverElem = request.getElement(AdminConstants.E_SERVER);
	    String serverName = serverElem.getAttribute(AdminConstants.A_NAME);
	    
	    Server server = prov.get(Key.ServerBy.name, serverName);
	    if (server == null) {
	    	throw ServiceException.INVALID_REQUEST("server with name " + serverName + " could not be found", null);
	    }
	    
	    checkRight(zsc, context, server, Admin.R_manageMailQueue);
	    
        RemoteManager rmgr = RemoteManager.getRemoteManager(server);
        RemoteResult rr = rmgr.execute(RemoteCommands.ZMQSTAT_ALL);
        Map<String,String> queueInfo;
        try { 
            queueInfo = RemoteResultParser.parseSingleMap(rr);
        } catch (IOException ioe) {
            throw ServiceException.FAILURE("exception occurred handling command", ioe);
        }
        if (queueInfo == null) {
            throw ServiceException.FAILURE("server " + serverName + " returned no result", null);
        }

        Element response = zsc.createElement(AdminConstants.GET_MAIL_QUEUE_INFO_RESPONSE);
        serverElem = response.addElement(AdminConstants.E_SERVER);
        serverElem.addAttribute(AdminConstants.A_NAME, serverName);
        for (String k : queueInfo.keySet()) {
            Element queue = serverElem.addElement(AdminConstants.E_QUEUE);
            queue.addAttribute(AdminConstants.A_NAME, k);
            queue.addAttribute(AdminConstants.A_N, queueInfo.get(k));
        }
        return response;
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_manageMailQueue);
    }
}
