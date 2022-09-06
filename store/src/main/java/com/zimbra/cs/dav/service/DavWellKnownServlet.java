// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DavWellKnownServlet extends ZimbraServlet {

  public void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(req);
    ZimbraLog.addUserAgentToContext(req.getHeader(DavProtocol.HEADER_USER_AGENT));
    String path = req.getPathInfo();
    if (path.equalsIgnoreCase("/caldav") || path.equalsIgnoreCase("/carddav")) {
      resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      resp.setHeader(
          "Location",
          req.getScheme()
              + "://"
              + req.getServerName()
              + ":"
              + req.getServerPort()
              + DavServlet.DAV_PATH);
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
