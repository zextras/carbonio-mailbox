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

public class LmcTagActionRequest extends LmcSoapRequest {

    private String mIDList;
    private String mOp;
    private String mColor;
    private String mName;


    public void setTagList(String idList) { mIDList = idList; }
    public void setOp(String op) { mOp = op; }
    public void setName(String t) { mName = t; }
    public void setColor(String c) { mColor = c; }

    public String getTagList() { return mIDList; }
    public String getOp() { return mOp; }
    public String getColor() { return mColor; }
    public String getName() { return mName; }


    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.TAG_ACTION_REQUEST);
        Element a = DomUtil.add(request, MailConstants.E_ACTION, "");
        DomUtil.addAttr(a, MailConstants.A_OPERATION, mOp);
        DomUtil.addAttr(a, MailConstants.A_ID, mIDList);
        DomUtil.addAttr(a, MailConstants.A_NAME, mName);
        DomUtil.addAttr(a, MailConstants.A_COLOR, mColor);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        LmcTagActionResponse response = new LmcTagActionResponse();
        Element a = DomUtil.get(responseXML, MailConstants.E_ACTION);
        response.setOp(DomUtil.getAttr(a, MailConstants.A_OPERATION));
        response.setTagList(DomUtil.getAttr(a, MailConstants.A_ID));
        return response;
    }
}
