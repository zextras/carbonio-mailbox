// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;

import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.resource.CalendarCollection;
import com.zimbra.cs.dav.resource.Collection;
import com.zimbra.cs.dav.resource.UrlNamespace;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.mailbox.MailItem;

public class MkCalendar extends DavMethod {
    public static final String MKCALENDAR = "MKCALENDAR";

    @Override
    public String getName() {
        return MKCALENDAR;
    }

    // valid return codes:
    // 201 Created, 207 Multi-Status (403, 409, 423, 424, 507),
    // 403 Forbidden, 409 Conflict, 415 Unsupported Media Type,
    // 507 Insufficient Storage
    @Override
    public void handle(DavContext ctxt) throws DavException, IOException {
        String user = ctxt.getUser();
        String name = ctxt.getItem();

        if (user == null || name == null)
            throw new DavException("invalid uri", HttpServletResponse.SC_FORBIDDEN, null);
        Element top = null;
        if (ctxt.hasRequestMessage()) {
            Document doc = ctxt.getRequestMessage();
            top = doc.getRootElement();
            if (!top.getName().equals(DavElements.P_MKCALENDAR))
                throw new DavException("msg "+top.getName()+" not allowed in MKCALENDAR", HttpServletResponse.SC_BAD_REQUEST, null);
        }

        Collection col = UrlNamespace.getCollectionAtUrl(ctxt, ctxt.getPath());
        if (col instanceof CalendarCollection)
            throw new DavException("can't create calendar under another calendar", HttpServletResponse.SC_FORBIDDEN, null);

        Collection newone = col.mkCol(ctxt, name, MailItem.Type.APPOINTMENT);
        boolean success = false;
        try {
            PropPatch.handlePropertyUpdate(ctxt, top, newone, true, MKCALENDAR);
            success = true;
        } finally {
            if (!success)
                newone.delete(ctxt);
        }
        ctxt.setStatus(HttpServletResponse.SC_CREATED);
        ctxt.getResponse().addHeader(DavProtocol.HEADER_CACHE_CONTROL, DavProtocol.NO_CACHE);
    }

    @Override
    public void checkPrecondition(DavContext ctxt) throws DavException {
        // DAV:resource-must-be-null
        // CALDAV:calendar-collection-location-ok
        // CALDAV:valid-calendar-data
        // DAV:need-privilege
    }

    @Override
    public void checkPostcondition(DavContext ctxt) throws DavException {
        // DAV:initialize-calendar-collection
    }
}
