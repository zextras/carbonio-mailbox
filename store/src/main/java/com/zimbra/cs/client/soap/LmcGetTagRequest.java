// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;

public class LmcGetTagRequest extends LmcSoapRequest {

    protected Element getRequestXML() {
        Element request = DocumentHelper.createElement(MailConstants.GET_TAG_REQUEST);
        return request;
    }

    protected LmcSoapResponse parseResponseXML(Element responseXML)
        throws ServiceException
    {
        // iterate over all the <tag> elements in the response
        ArrayList tags = new ArrayList();
        for (Iterator ait = responseXML.elementIterator(MailConstants.E_TAG); ait.hasNext(); ) {
            Element a = (Element) ait.next();
            tags.add(parseTag(a));
        }

        // create the response object and put in the tags
        LmcGetTagResponse response = new LmcGetTagResponse();
        response.setTags(tags);
        return response;
    }
}
