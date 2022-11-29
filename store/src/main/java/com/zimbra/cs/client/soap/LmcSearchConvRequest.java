// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.Element;

import com.zimbra.common.soap.DomUtil;
import com.zimbra.common.soap.MailConstants;

public class LmcSearchConvRequest extends LmcSearchRequest {

    private String mConvID;

    public void setConvID(String c) { mConvID = c; }

    public String getConvID() { return mConvID; }

    protected Element getRequestXML() {
        // the request XML is the same as for search, with a conversation ID added
        Element response = createQuery(MailConstants.SEARCH_CONV_REQUEST);
        DomUtil.addAttr(response, MailConstants.A_CONV_ID, mConvID);
        return response;
    }


}
