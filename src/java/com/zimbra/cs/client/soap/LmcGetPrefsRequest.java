/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006 Zimbra, Inc.
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

package com.zimbra.cs.client.soap;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.account.AccountService;


public class LmcGetPrefsRequest extends LmcSoapRequest {

    private String mPrefsToGet[];
    

    /**
     * Set the preferences to retrieve.
     * @param prefsToGet[] - array of names of prefs to get.  Pass in null 
     * for all preferences
     */
    public void setPrefsToGet(String prefsToGet[]) { mPrefsToGet = prefsToGet; }
    
    public String[] getPrefsToGet() { return mPrefsToGet; }

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(AccountService.GET_PREFS_REQUEST);
        if (mPrefsToGet != null) {
            for (int i = 0; i < mPrefsToGet.length; i++) {
                    Element pe = DomUtil.add(request, AccountService.E_PREF, "");  
                    DomUtil.addAttr(pe, AccountService.A_NAME, mPrefsToGet[i]);  
            }
        }
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML) 
        throws ServiceException
    {
        // iterate over all the <pref> elements in the response
        HashMap prefMap = new HashMap();
        for (Iterator ait = responseXML.elementIterator(AccountService.E_PREF); ait.hasNext(); ) {
            Element a = (Element) ait.next();
            addPrefToMultiMap(prefMap, a);
        }

        // create the response object and put in the HashMap
        LmcGetPrefsResponse response = new LmcGetPrefsResponse();
        response.setPrefsMap(prefMap);
        return response;
    }

}
