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

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class DeleteCos extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
	    Provisioning prov = Provisioning.getInstance();

	    String id = request.getElement(AdminConstants.E_ID).getText();

	    Cos cos = prov.get(Key.CosBy.id, id);
        if (cos == null)
            throw AccountServiceException.NO_SUCH_COS(id);
        
        checkRight(zsc, context, cos, Admin.R_deleteCos);
        
        prov.deleteCos(cos.getId());

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "DeleteCos","name", cos.getName(), "id", cos.getId()}));

	    Element response = zsc.createElement(AdminConstants.DELETE_COS_RESPONSE);
	    return response;
	}

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_deleteCos);
    }
}