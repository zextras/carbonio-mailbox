package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;

public class ClearCookie extends AdminDocumentHandler {
    
    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        HttpServletResponse servletResp = (HttpServletResponse)context.get(SoapServlet.SERVLET_RESPONSE);
        
        for (Element eCookie : request.listElements(AdminConstants.E_COOKIE)) {
            String cookie = eCookie.getAttribute(AdminConstants.A_NAME);
            ZimbraCookie.clearCookie(servletResp, cookie);
        }
        
        Element resp = zsc.createElement(AdminConstants.CLEAR_COOKIE_RESPONSE);
        return resp;
    }
    
    public boolean needsAdminAuth(Map<String, Object> context) {
        return false;
    }

    public boolean needsAuth(Map<String, Object> context) {
        return false;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    }
}
