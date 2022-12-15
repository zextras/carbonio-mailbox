// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.XMPPComponentBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.XMPPComponent;
import com.zimbra.cs.account.AccessManager.AttrRightChecker;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * <GetXMPPComponentRequest>
 *    <xmppcomponent by="by">identifier</xmppcomponent>
 * </GetXMPPComponentRequest>   
 */
public class GetXMPPComponent extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        
        Set<String> reqAttrs = getReqAttrs(request, AttributeClass.xmppComponent);
        Element id = request.getElement(AdminConstants.E_XMPP_COMPONENT);
        String byStr = id.getAttribute(AdminConstants.A_BY);
        String name = id.getText();
        
        if (name == null || name.equals(""))
            throw ServiceException.INVALID_REQUEST("must specify a value for a xmppcomponent", null);
        
        Key.XMPPComponentBy by = Key.XMPPComponentBy.valueOf(byStr);
        
        XMPPComponent comp = prov.get(by, name);
        if (comp == null)
            throw AccountServiceException.NO_SUCH_XMPP_COMPONENT(name);
        
        AdminAccessControl aac = checkRight(zsc, context, comp, AdminRight.PR_ALWAYS_ALLOW);
        
        Element response = zsc.createElement(AdminConstants.GET_XMPPCOMPONENT_RESPONSE);
        encodeXMPPComponent(response, comp, reqAttrs, aac.getAttrRightChecker(comp));
        return response;
    }
    
    public static Element encodeXMPPComponent(Element parent, XMPPComponent comp) {
        return encodeXMPPComponent(parent, comp, null, null);
    }
    
    public static Element encodeXMPPComponent(Element parent, XMPPComponent comp,
            Set<String> reqAttrs, AttrRightChecker attrRightChecker) {
        Element e = parent.addElement(AccountConstants.E_XMPP_COMPONENT);
        e.addAttribute(AccountConstants.A_NAME, comp.getName());
        e.addAttribute(AccountConstants.A_ID, comp.getId());
        
        try { // for testing only
            e.addAttribute("x-domainName", comp.getDomain().getName());
        } catch (ServiceException ex) {}

        try { // for testing only
            e.addAttribute("x-serverName", comp.getServer().getName());
        } catch (ServiceException ex) {}
        
        ToXML.encodeAttrs(e, comp.getUnicodeAttrs(), reqAttrs, attrRightChecker);
        return e;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getXMPPComponent);
        notes.add(String.format(AdminRightCheckPoint.Notes.GET_ENTRY, Admin.R_getXMPPComponent.getName()));
    }

}
