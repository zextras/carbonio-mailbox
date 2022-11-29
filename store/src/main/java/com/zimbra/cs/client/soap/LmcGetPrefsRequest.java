// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;


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
        Element request = DocumentHelper.createElement(AccountConstants.GET_PREFS_REQUEST);
        if (mPrefsToGet != null) {
            for (int i = 0; i < mPrefsToGet.length; i++) {
                    Element pe = DomUtil.add(request, AccountConstants.E_PREF, "");
                    DomUtil.addAttr(pe, AccountConstants.A_NAME, mPrefsToGet[i]);
            }
        }
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        // iterate over all the <pref> elements in the response
        HashMap prefMap = new HashMap();
        for (Iterator ait = responseXML.elementIterator(AccountConstants.E_PREF); ait.hasNext(); ) {
            Element a = (Element) ait.next();
            addPrefToMultiMap(prefMap, a);
        }

        // create the response object and put in the HashMap
        LmcGetPrefsResponse response = new LmcGetPrefsResponse();
        response.setPrefsMap(prefMap);
        return response;
    }

}
