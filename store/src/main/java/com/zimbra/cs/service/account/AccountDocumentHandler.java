// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.MailMode;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.SoapServlet;


public abstract class AccountDocumentHandler extends DocumentHandler {

    @Override
    protected Element proxyIfNecessary(Element request, Map<String, Object> context) throws ServiceException {
        try {
            // by default, try to execute on the appropriate host
            return super.proxyIfNecessary(request, context);
        } catch (ServiceException e) {
            // if something went wrong proxying the request, just execute it locally
            if (ServiceException.PROXY_ERROR.equals(e.getCode()))
                return null;
            // but if it's a real error, it's a real error
            throw e;
        }
    }
    
    /*
     * bug 27389
     */
    protected boolean checkPasswordSecurity(Map<String, Object> context) throws ServiceException {
        HttpServletRequest req = (HttpServletRequest)context.get(SoapServlet.SERVLET_REQUEST);
        boolean isHttps = req.getScheme().equals("https");
        if (isHttps)
            return true;
        
        // clear text
        Server server = Provisioning.getInstance().getLocalServer();
        String modeString = server.getAttr(Provisioning.A_zimbraMailMode, null);
        if (modeString == null) {
            // not likely, but just log and let it through
            ZimbraLog.soap.warn("missing " + Provisioning.A_zimbraMailMode + 
                                " for checking password security, allowing the request");
            return true;
        }
            
        MailMode mailMode = Provisioning.MailMode.fromString(modeString);
        if (mailMode == MailMode.mixed && 
            !server.getBooleanAttr(Provisioning.A_zimbraMailClearTextPasswordEnabled, true)) 
            return false;
        else
            return true;
    }
    
    protected Set<String> getReqAttrs(Element request, AttributeClass klass) throws ServiceException {
        String attrsStr = request.getAttribute(AccountConstants.A_ATTRS, null);
        if (attrsStr == null) {
            return null;
        }
        
        String[] attrs = attrsStr.split(",");

        Set<String> attrsOnEntry = AttributeManager.getInstance().getAllAttrsInClass(klass);
        Set<String> validAttrs = new HashSet<>();

        for (String attr : attrs) {
            if (attrsOnEntry.contains(attr)) {
                validAttrs.add(attr);
            } else {
                throw ServiceException.INVALID_REQUEST("requested attribute " + attr + 
                        " is not on " + klass.name(), null);
            }
        }

        // check and throw if validAttrs is empty?
        // probably not, to be compatible with SearchDirectory

        return validAttrs;
    }
}
