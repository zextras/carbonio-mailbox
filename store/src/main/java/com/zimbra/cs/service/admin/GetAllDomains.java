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
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class GetAllDomains extends AdminDocumentHandler {

    public static final String BY_NAME = "name";
    public static final String BY_ID = "id";
    
	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
	    
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        boolean applyConfig = request.getAttributeBool(AdminConstants.A_APPLY_CONFIG, true);
        List domains = prov.getAllDomains();
        
        AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);
        
        Element response = zsc.createElement(AdminConstants.GET_ALL_DOMAINS_RESPONSE);
        for (Iterator it = domains.iterator(); it.hasNext(); ) {
            Domain domain = (Domain) it.next();
            
            if (aac.hasRightsToList(domain, Admin.R_listDomain, null))
                GetDomain.encodeDomain(response, domain, applyConfig, null, aac.getAttrRightChecker(domain));
        }

	    return response;
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listDomain);
        relatedRights.add(Admin.R_getDomain);
        
        notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
    }
}
