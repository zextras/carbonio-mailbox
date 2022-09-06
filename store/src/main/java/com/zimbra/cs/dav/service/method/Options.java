// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavMethod;
import com.zimbra.cs.dav.service.DavServlet;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class Options extends DavMethod {
  public static final String OPTIONS = "OPTIONS";

  public String getName() {
    return OPTIONS;
  }

  public void handle(DavContext ctxt) throws DavException, IOException, ServiceException {
    HttpServletResponse resp = ctxt.getResponse();
    DavServlet.setAllowHeader(resp);
    if (ctxt.isMsft()) resp.addHeader(DavProtocol.HEADER_MS_AUTHOR_VIA, "DAV");
    resp.setContentLength(0);
    try {
      DavResource rs = ctxt.getRequestedResource();
      ctxt.setDavCompliance(DavProtocol.getComplianceString(rs.getComplianceList()));
    } catch (Exception e) {
    }
    sendResponse(ctxt);
  }
}
