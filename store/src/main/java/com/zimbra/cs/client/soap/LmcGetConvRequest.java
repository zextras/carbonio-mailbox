// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.client.*;


public class LmcGetConvRequest extends LmcSoapRequest {

    private String mConvID;
    private String mMsgsToGet[];
    
    // There is a single conversation to get.  Must be present.
    public void setConvToGet(String f) { mConvID = f; }

    // Set the ID's of the msgs within the conversation to get.  Optional.
    public void setMsgsToGet(String m[]) { mMsgsToGet = m; }
    
    public String getConvToGet() { return mConvID; }

    public String[] getMsgsToGet() { return mMsgsToGet; }


    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.GET_CONV_REQUEST);

        // set the ID of the conversation to get
        Element convElement = DomUtil.add(request, MailConstants.E_CONV, "");
        DomUtil.addAttr(convElement, MailConstants.A_ID, mConvID);

        // add message elements within the conversation element if desired
        if (mMsgsToGet != null) {
          for (String s : mMsgsToGet) {
            Element m = DomUtil.add(convElement, MailConstants.E_MSG, "");
            DomUtil.addAttr(m, MailConstants.A_ID, s);
          }
        }

        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML) 
        throws ServiceException, LmcSoapClientException
    {
        // the response will always be exactly one conversation
        LmcConversation c = parseConversation(DomUtil.get(responseXML, MailConstants.E_CONV));
        LmcGetConvResponse response = new LmcGetConvResponse();
        response.setConv(c);
        return response;
    }

}
