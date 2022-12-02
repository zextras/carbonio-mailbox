// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.service.DavMethod;

public class Report extends DavMethod {
    public static final String REPORT = "REPORT";

    @Override
    public String getName() {
        return REPORT;
    }

    private static HashMap<QName,DavMethod> sReports;

    static {
        sReports = new HashMap<QName,DavMethod>();
        sReports.put(DavElements.E_CALENDAR_QUERY, new CalendarQuery());
        sReports.put(DavElements.E_CALENDAR_MULTIGET, new CalendarMultiget());
        sReports.put(DavElements.E_FREE_BUSY_QUERY, new FreeBusyQuery());
        sReports.put(DavElements.E_PRINCIPAL_PROPERTY_SEARCH, new AclReports());
        sReports.put(DavElements.E_ACL_PRINCIPAL_PROP_SET, new AclReports());
        sReports.put(DavElements.E_PRINCIPAL_MATCH, new AclReports());
        sReports.put(DavElements.E_PRINCIPAL_SEARCH_PROPERTY_SET, new AclReports());
        sReports.put(DavElements.E_EXPAND_PROPERTY, new ExpandProperty());
        sReports.put(DavElements.CardDav.E_ADDRESSBOOK_QUERY, new AddressbookQuery());
        sReports.put(DavElements.CardDav.E_ADDRESSBOOK_MULTIGET, new AddressbookMultiget());
    }

    @Override
    public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
        if (!ctxt.hasRequestMessage()) {
            throw new DavException("empty request body", HttpServletResponse.SC_BAD_REQUEST, null);
        }

        Document req = ctxt.getRequestMessage();
        Element top = req.getRootElement();
        if (top == null) {
            throw new DavException("empty request body", HttpServletResponse.SC_BAD_REQUEST, null);
        }
        QName topName = top.getQName();
        DavMethod report = sReports.get(topName);
        if (report == null) {
            throw new DavException.UnsupportedReport(topName);
        }

        disableJettyTimeout(ctxt);
        if (ctxt.getDepth() != DavContext.Depth.zero) {
            ctxt.getDavResponse().createResponse(ctxt);
        }
        report.handle(ctxt);
        sendResponse(ctxt);
    }
}
