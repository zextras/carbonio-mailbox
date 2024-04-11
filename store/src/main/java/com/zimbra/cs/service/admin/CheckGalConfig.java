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
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.gal.GalOp;
import com.zimbra.cs.service.mail.ToXML;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class CheckGalConfig extends AdminDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        
        Element q = request.getOptionalElement(AdminConstants.E_QUERY);
        String query = null;
        long limit = 0;
        if (q != null) {
            query = q.getText();
            limit = q.getAttributeLong(AdminConstants.A_LIMIT, 10);
        }
        
        Element action = request.getOptionalElement(AdminConstants.E_ACTION);
        GalOp galOp = GalOp.search;
        if (action != null)
            galOp = GalOp.fromString(action.getText());
                
	    Map attrs = AdminService.getAttrs(request, true);

        Element response = zsc.createElement(AdminConstants.CHECK_GAL_CONFIG_RESPONSE);
        Provisioning.Result r = Provisioning.getInstance().checkGalConfig(
                attrs, query, (int)limit, galOp);
        
        response.addElement(AdminConstants.E_CODE).addText(r.getCode());
        String message = r.getMessage();
        if (message != null)
            response.addElement(AdminConstants.E_MESSAGE).addText(message);

        if (r instanceof Provisioning.GalResult) {
            List<GalContact> contacts = ((Provisioning.GalResult)r).getContacts();
            if (contacts != null) {
                for (GalContact contact : contacts) {
                    ToXML.encodeGalContact(response, contact);
                }
            }
        }
	    return response;
	}
	
	@Override
	public void docRights(List<AdminRight> relatedRights, List<String> notes) {
	    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    }
}
