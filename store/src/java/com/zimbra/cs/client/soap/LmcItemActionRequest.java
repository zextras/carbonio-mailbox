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

public class LmcItemActionRequest extends LmcSoapRequest {

    protected String mIDList;
    protected String mOp;
    protected String mTag;
    protected String mFolder;


    /**
     * Set the list of Msg ID's to operate on
     * @param idList - a list of the messages to operate on
     */
    public void setMsgList(String idList) { mIDList = idList; }

    /**
     * Set the operation
     * @param op - the operation (delete, read, etc.)
     */
    public void setOp(String op) { mOp = op; }

    public void setTag(String t) { mTag = t; }

    public void setFolder(String f) { mFolder = f; }

    public String getMsgList() { return mIDList; }
    public String getOp() { return mOp; }
    public String getTag() { return mTag; }
    public String getFolder() { return mFolder; }

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.ITEM_ACTION_REQUEST);
        Element a = DomUtil.add(request, MailConstants.E_ACTION, "");
        DomUtil.addAttr(a, MailConstants.A_ID, mIDList);
        DomUtil.addAttr(a, MailConstants.A_OPERATION, mOp);
        DomUtil.addAttr(a, MailConstants.A_TAG, mTag);
        DomUtil.addAttr(a, MailConstants.A_FOLDER, mFolder);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        LmcMsgActionResponse response = new LmcMsgActionResponse();
        Element a = DomUtil.get(responseXML, MailConstants.E_ACTION);
        response.setMsgList(DomUtil.getAttr(a, MailConstants.A_ID));
        response.setOp(DomUtil.getAttr(a, MailConstants.A_OPERATION));
        return response;
    }

}
