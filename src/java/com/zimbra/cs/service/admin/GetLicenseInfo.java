/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006 Zimbra, Inc.
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

import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

import com.zimbra.common.localconfig.LC;

public class GetLicenseInfo extends AdminDocumentHandler {

    static final String TRIAL_EXPIRATION_DATE_KEY = "trial_expiration_date";
    
    public Element handle(Element request, Map<String, Object> context) {
        ZimbraSoapContext lc = getZimbraSoapContext(context);

        String expirationDate = LC.get(TRIAL_EXPIRATION_DATE_KEY);
        Element response = lc.createElement(AdminService.GET_LICENSE_INFO_RESPONSE);
        Element el = response.addElement(AdminService.E_LICENSE_EXPIRATION);
        el.addAttribute(AdminService.A_LICENSE_EXPIRATION_DATE, expirationDate);
        return response;
    }

    public boolean needsAdminAuth(Map<String, Object> context) {
        return false;
    }

    public boolean needsAuth(Map<String, Object> context) {
        return false;
    }


}
