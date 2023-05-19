// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;

import org.dom4j.Element;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavContext.RequestProp;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.CalendarCollection;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavResponse;

/*
 * draft-dusseault-caldav section 9.10
 * 
 *     <!ELEMENT calendar-multiget ((DAV:allprop |
 *                                   DAV:propname |
 *                                   DAV:prop)?, DAV:href+)>
 *                                
 */
public class CalendarMultiget extends Report {
	public void handle(DavContext ctxt) throws ServiceException, DavException {
		Element query = ctxt.getRequestMessage().getRootElement();
		if (!query.getQName().equals(DavElements.E_CALENDAR_MULTIGET))
			throw new DavException("msg "+query.getName()+" is not calendar-multiget", HttpServletResponse.SC_BAD_REQUEST, null);

		DavResponse resp = ctxt.getDavResponse();
		ArrayList<String> hrefs = new ArrayList<>();
		for (Object obj : query.elements(DavElements.E_HREF))
			if (obj instanceof Element)
				hrefs.add(((Element)obj).getText());
		long ts = System.currentTimeMillis();
		DavResource reqResource = ctxt.getRequestedResource();
		if (!(reqResource instanceof CalendarCollection))
			throw new DavException("requested resource is not a calendar collection", HttpServletResponse.SC_BAD_REQUEST, null);
		CalendarCollection calResource = (CalendarCollection) reqResource;
		long now = System.currentTimeMillis();
		ZimbraLog.dav.debug("GetRequestedResource: "+(now - ts)+"ms");
		RequestProp reqProp = ctxt.getRequestProp();
		for (DavResource rs : calResource.getAppointmentsByUids(ctxt, hrefs))
			resp.addResource(ctxt, rs, reqProp, false);
		ts = now;
		now = System.currentTimeMillis();
		ZimbraLog.dav.debug("multiget: "+(now - ts)+"ms");
	}
}
