// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

public class LmcPingRequest extends LmcSoapRequest {

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(AdminConstants.PING_REQUEST);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        // there is no response to the request, only a fault
        LmcPingResponse response = new LmcPingResponse();
        return response;
    }
}