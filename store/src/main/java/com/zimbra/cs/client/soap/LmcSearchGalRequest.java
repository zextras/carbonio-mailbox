// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.cs.client.*;

public class LmcSearchGalRequest extends LmcSoapRequest {

    private String mName;
    
    public void setName(String n) { mName = n; }
    
    public String getName() { return mName; }
    
	protected Element getRequestXML() throws LmcSoapClientException {
		Element request = DocumentHelper.createElement(AccountConstants.SEARCH_GAL_REQUEST);
		DomUtil.add(request, AccountConstants.E_NAME, mName);
        return request;
    }

	protected LmcSoapResponse parseResponseXML(Element responseXML)
	    throws SoapParseException, ServiceException, LmcSoapClientException 
    {
        LmcContact contacts[] = parseContactArray(responseXML);
        LmcSearchGalResponse sgResp = new LmcSearchGalResponse();
        sgResp.setContacts(contacts);
        return sgResp;
	}

}
