// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.mime.ContentType;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.dav.service.DavServlet;
import com.zimbra.cs.servlet.ETagHeaderFilter;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Get extends DavMethod {
  public static final String GET = "GET";

  @Override
  public String getName() {
    return GET;
  }

  protected boolean returnContent() {
    return true;
  }

  @Override
  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    DavResource resource = ctxt.getRequestedResource();
    HttpServletResponse resp = ctxt.getResponse();
    String contentType = resource.getContentType(ctxt);
    if (contentType != null) {
      ContentType ct = new ContentType(contentType);
      if (ct.getParameter(MimeConstants.P_CHARSET) == null)
        ct.setParameter(MimeConstants.P_CHARSET, MimeConstants.P_CHARSET_UTF8);
      resp.setContentType(ct.toString());
    }
    if (resource.hasEtag()) {
      ctxt.getResponse().setHeader(DavProtocol.HEADER_ETAG, resource.getEtag());
      ctxt.getResponse().setHeader(ETagHeaderFilter.ZIMBRA_ETAG_HEADER, resource.getEtag());
    }

    // in some cases getContentLength() returns an estimate, and the exact
    // content length is not known until DavResource.getContent() is called.
    // the estimate is good enough for PROPFIND, but not when returning
    // the contents. just leave off setting content length explicitly,
    // and have the servlet deal with it by doing chunking or
    // setting content-length header on its own.

    // resp.setContentLength(resource.getContentLength());
    if (!returnContent() || !resource.hasContent(ctxt)) return;
    resp.setHeader("Content-Disposition", "attachment");
    ByteUtil.copy(resource.getContent(ctxt), true, ctxt.getResponse().getOutputStream(), false);
    resp.setStatus(ctxt.getStatus());
    ctxt.responseSent();
    if (ZimbraLog.dav.isDebugEnabled()) {
      StringBuilder sb =
          new StringBuilder("Response for DAV GET ").append(ctxt.getUri()).append("\n");
      if (contentType != null && contentType.startsWith("text")) {
        DavServlet.addResponseHeaderLoggingInfo(resp, sb);
        if (ZimbraLog.dav.isTraceEnabled()) {
          sb.append(new String(ByteUtil.getContent(resource.getContent(ctxt), 0), "UTF-8"));
        }
        ZimbraLog.dav.debug(sb);
      }
    }
  }
}
