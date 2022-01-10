// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.dav.service.DavServlet;

public class Post extends DavMethod {
    public static final String POST  = "POST";

    @Override
    public String getName() {
        return POST;
    }

    @Override
    public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
        String user = ctxt.getUser();
        String name = ctxt.getItem();

        if (user == null || name == null) {
            throw new DavException("invalid uri", HttpServletResponse.SC_NOT_FOUND);
        }

        DavResource rs = ctxt.getRequestedResource();
        rs.handlePost(ctxt);
        sendResponse(ctxt);
        if (ZimbraLog.dav.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Response for DAV POST ").append(ctxt.getUri()).append("\n");
            DavServlet.addResponseHeaderLoggingInfo(ctxt.getResponse(), sb);
            ZimbraLog.dav.debug(sb);
        }
    }
}
