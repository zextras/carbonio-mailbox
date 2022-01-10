// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.XMPPComponent;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * 
 */
public class GetAllXMPPComponents extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        
        List<XMPPComponent> components = prov.getAllXMPPComponents();
        
        AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);
        
        Element response = zsc.createElement(AdminConstants.GET_ALL_XMPPCOMPONENTS_REQUEST);
        
        for (XMPPComponent comp : components) {
            if (aac.hasRightsToList(comp, Admin.R_listXMPPComponent, null))
                GetXMPPComponent.encodeXMPPComponent(response, comp, null, aac.getAttrRightChecker(comp));
        }
        
        return response;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listXMPPComponent);
        relatedRights.add(Admin.R_getXMPPComponent);
        
        notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
    }
}
