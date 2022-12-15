// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.resource.Collection;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.resource.UrlNamespace;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.servlet.ETagHeaderFilter;

public class Put extends DavMethod {
    public static final String PUT = "PUT";

    @Override
    public String getName() {
        return PUT;
    }

    @Override
    public void handle(DavContext ctxt) throws DavException, IOException {
        String user = ctxt.getUser();
        String name = ctxt.getItem();

        if (user == null || name == null)
            throw new DavException("invalid uri", HttpServletResponse.SC_NOT_ACCEPTABLE, null);

        Collection col = UrlNamespace.getCollectionAtUrl(ctxt, ctxt.getPath());
        DavResource rs = col.createItem(ctxt, name);
        if (rs.isNewlyCreated())
            ctxt.setStatus(HttpServletResponse.SC_CREATED);
        else
            ctxt.setStatus(HttpServletResponse.SC_NO_CONTENT);
        if (rs.hasEtag()) {
            ctxt.getResponse().setHeader(DavProtocol.HEADER_ETAG, rs.getEtag());
            ctxt.getResponse().setHeader(ETagHeaderFilter.ZIMBRA_ETAG_HEADER, rs.getEtag());
        }
    }
}
