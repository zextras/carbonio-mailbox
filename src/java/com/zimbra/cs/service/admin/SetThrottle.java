/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.admin;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.operation.Scheduler;
import com.zimbra.soap.Element;
import com.zimbra.soap.SoapFaultException;
import com.zimbra.soap.ZimbraSoapContext;

public class SetThrottle extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
                throws ServiceException, SoapFaultException {

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        
        String concurStr = request.getAttribute(AdminService.A_CONCURRENCY, null);
        
        if (concurStr != null) {
            int[] params = Scheduler.readOpsFromString(concurStr);
            if (params == null) 
                throw ServiceException.INVALID_REQUEST("Could not parse concurrency string: "+concurStr, null);
            Scheduler.setConcurrency(params);    
        }
        
        Element response = lc.createElement(AdminService.SET_THROTTLE_RESPOSNE);
        
        Scheduler s = Scheduler.get(null);
        concurStr = "";
        int[] concur = s.getMaxOps();
        for (int i = 0; i < concur.length; i++) {
            if (i > 0)
                concurStr+=",";
            concurStr += concur[i];
        }
        response.addAttribute(AdminService.A_CONCURRENCY, concurStr);
        
        return response;
    }
    
}
