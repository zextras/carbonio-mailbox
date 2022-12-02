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


public class LmcGetMsgRequest extends LmcSoapRequest {

    private String mMsgID;
    private String mRead;

    // There is a single msg to get.  Must be present.
    public void setMsgToGet(String f) { mMsgID = f; }

    // Optionally set read
    public void setRead(String r) { mRead = r; }

    public String getMsgToGet() { return mMsgID; }

    public String getRead() { return mRead; }


    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.GET_MSG_REQUEST);
        Element m = DomUtil.add(request, MailConstants.E_MSG, "");
        DomUtil.addAttr(m, MailConstants.A_ID, mMsgID);
        addAttrNotNull(m, MailConstants.A_MARK_READ, mRead);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException, LmcSoapClientException
    {
        LmcGetMsgResponse response = new LmcGetMsgResponse();
        response.setMsg(parseMessage(DomUtil.get(responseXML, MailConstants.E_MSG)));
        return response;
    }
}
