// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class DeleteDomain extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        String id = request.getAttribute(AdminConstants.E_ID);

        Domain domain = prov.get(Key.DomainBy.id, id);
        if (domain == null)
            throw AccountServiceException.NO_SUCH_DOMAIN(id);
        
        if (domain.isShutdown())
            throw ServiceException.PERM_DENIED("can not access domain, domain is in " + domain.getDomainStatusAsString() + " status");
        
        checkRight(zsc, context, domain, Admin.R_deleteDomain);
        
        String name = domain.getName();
        
        prov.deleteDomain(id);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(new String[] {"cmd", "DeleteDomain","name", name, "id", id }));

	    Element response = zsc.createElement(AdminConstants.DELETE_DOMAIN_RESPONSE);
	    return response;
	}
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_deleteDomain);
    }
}