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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class CreateCos extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
	    Provisioning prov = Provisioning.getInstance();

	    String name = request.getElement(AdminConstants.E_NAME).getText().toLowerCase();
	    Map<String, Object> attrs = AdminService.getAttrs(request);

	    checkRight(zsc, context, null, Admin.R_createCos);
	    checkSetAttrsOnCreate(zsc, TargetType.cos, name, attrs);

	    Cos cos = prov.createCos(name, attrs);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "CreateCos","name", name}, attrs));

	    Element response = zsc.createElement(AdminConstants.CREATE_COS_RESPONSE);
	    GetCos.encodeCos(response, cos);

	    return response;
	}

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_createCos);
        notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY,
                Admin.R_modifyCos.getName(), "cos"));
    }
}