// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.mail.Part;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CollectConfigFiles extends ZimbraServlet {
  private static final String P_HOST = "host";
  private static final String DOWNLOAD_CONTENT_TYPE = "application/x-compressed";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    try {
      // check the auth token
      AuthToken authToken = getAdminAuthTokenFromCookie(req, resp);
      if (authToken == null) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      if (!authToken.isAdmin()) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      // take the host name
      Provisioning prov = Provisioning.getInstance();
      String hostName = req.getParameter(P_HOST);
      Server server = prov.get(Key.ServerBy.name, hostName);
      if (server == null) {
        throw ServiceException.INVALID_REQUEST(
            "server with name " + hostName + " could not be found", null);
      }
      // call RemoteManager
      RemoteManager rmgr = RemoteManager.getRemoteManager(server);
      RemoteResult rr = rmgr.execute(RemoteCommands.COLLECT_CONFIG_FILES);
      // stream the data
      resp.setContentType(DOWNLOAD_CONTENT_TYPE);
      ContentDisposition cd =
          new ContentDisposition(Part.INLINE).setParameter("filename", hostName + ".conf.tgz");
      resp.addHeader("Content-Disposition", cd.toString());
      ByteUtil.copy(new ByteArrayInputStream(rr.getMStdout()), true, resp.getOutputStream(), false);
    } catch (ServiceException e) {
      returnError(resp, e);
      return;
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }
}
