// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2005. 3. 7.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.client.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapParseException;

/**
 * @author jhahm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LmcNoOpRequest extends LmcSoapRequest {

	/* (non-Javadoc)
	 * @see com.zimbra.cs.client.soap.LmcSoapRequest#getRequestXML()
	 */
	protected Element getRequestXML() throws LmcSoapClientException {
        Element request = DocumentHelper.createElement(MailConstants.NO_OP_REQUEST);
        return request;
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.client.soap.LmcSoapRequest#parseResponseXML(org.dom4j.Element)
	 */
	protected LmcSoapResponse parseResponseXML(Element responseXML)
			throws SoapParseException, ServiceException, LmcSoapClientException {
        LmcNoOpResponse response = new LmcNoOpResponse();
        return response;
	}
}
