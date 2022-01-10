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

public class LmcAddMsgRequest extends LmcSoapRequest {

    private LmcMessage mMsg;

    /**
     * Set the message that will be added
     * @param m - the message to be added
     */
    public void setMsg(LmcMessage m) { mMsg = m; }

    public LmcMessage getMsg() { return mMsg; }

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.ADD_MSG_REQUEST);
        addMsg(request, mMsg, null, null, null);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        Element m = DomUtil.get(responseXML, MailConstants.E_MSG);
        LmcAddMsgResponse response = new LmcAddMsgResponse();
        response.setID(DomUtil.getAttr(m, MailConstants.A_ID));
        return response;
    }

}

