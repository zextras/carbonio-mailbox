// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.XMPPComponent;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;


/**
 * 
 */
public class CreateXMPPComponent extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        
        // <CreateXMPPComponentRequest>
        //    <xmppComponent name="name">
        //       <domain [by="id, name, virtualHostname, krb5Realm"]>domainId</domain>
        //       <server[by="id, name, serviceHostname"]>serviceId</domain>
        //       <a n="zimbraXMPPComponentCategory">category (see XEP-0030)</a>
        //       <a n="zimbraXMPPComponentName">long component name</a>
        //       [<a n="zimbraXMPPComponentType">type from XEP-0030</a>]
        //    </xmppComponent>
        //
        
        Element cEl = request.getElement(AccountConstants.E_XMPP_COMPONENT);
        Map<String, Object> attrs = AdminService.getAttrs(cEl);
        
        Element domainElt = cEl.getElement(AdminConstants.E_DOMAIN);
        String byStr = domainElt.getAttribute(AdminConstants.A_BY, "id");
        Key.DomainBy domainby = Key.DomainBy.valueOf(byStr);
        Domain domain = Provisioning.getInstance().get(domainby,domainElt.getText());
        
        Element serverElt = cEl.getElement(AdminConstants.E_SERVER);
        String serverByStr = serverElt.getAttribute(AdminConstants.A_BY);
        Server server = prov.get(Key.ServerBy.fromString(serverByStr), serverElt.getText());
        
        String name = cEl.getAttribute(AccountConstants.A_NAME);
        
        if (!name.endsWith(domain.getName())) {
            throw ServiceException.INVALID_REQUEST("Specified component name must be full name, and must be a subdomain of the specified parent", null);
        }
        
        checkRight(zsc, context, null, Admin.R_createXMPPComponent);
        checkSetAttrsOnCreate(zsc, TargetType.xmppcomponent, name, attrs);
        
        XMPPComponent comp = prov.createXMPPComponent(name, domain, server, attrs);
        
        Element response = zsc.createElement(AdminConstants.CREATE_XMPPCOMPONENT_RESPONSE);
        GetXMPPComponent.encodeXMPPComponent(response, comp);
        return response;
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_createXMPPComponent);
        notes.add(String.format(AdminRightCheckPoint.Notes.MODIFY_ENTRY, 
                Admin.R_modifyXMPPComponent.getName(), "XMPP component"));
    }
}
