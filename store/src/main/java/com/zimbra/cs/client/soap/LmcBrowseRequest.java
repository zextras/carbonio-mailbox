// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.cs.client.*;

public class LmcBrowseRequest extends LmcSoapRequest {

    private String mBrowseBy;
    
    public void setBrowseBy(String b) { mBrowseBy = b; }
    
    public String getBrowseBy() { return mBrowseBy; }
    
	protected Element getRequestXML() throws LmcSoapClientException {
        Element request = DocumentHelper.createElement(MailConstants.BROWSE_REQUEST);
        DomUtil.addAttr(request, MailConstants.A_BROWSE_BY, mBrowseBy);
        return request;
	}

    protected LmcBrowseData parseBrowseData(Element bdElem) {
    	LmcBrowseData bd = new LmcBrowseData();
        bd.setFlags(bdElem.attributeValue(MailConstants.A_BROWSE_DOMAIN_HEADER));
        bd.setData(bdElem.getText());
        return bd;
    }
    
	protected LmcSoapResponse parseResponseXML(Element parentElem)
			throws SoapParseException, ServiceException, LmcSoapClientException 
    {
		LmcBrowseResponse response = new LmcBrowseResponse();
        ArrayList bdArray = new ArrayList();
        for (Iterator ait = parentElem.elementIterator(MailConstants.E_BROWSE_DATA); ait.hasNext(); ) {
            Element a = (Element) ait.next();
            bdArray.add(parseBrowseData(a));
        }

        if (!bdArray.isEmpty()) {
            LmcBrowseData[] bds = new LmcBrowseData[bdArray.size()];
            response.setData((LmcBrowseData []) bdArray.toArray(bds));
        } 

        return response;
	}

}
