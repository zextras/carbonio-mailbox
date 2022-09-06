// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.soap.util.WsdlGenerator;
import com.zimbra.soap.util.WsdlServiceInfo;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/** The wsdl service servlet - serves up files comprising Zimbra's WSDL definition */
public class WsdlServlet extends ZimbraServlet {

  /** */
  private static final long serialVersionUID = -2727046288266393825L;

  @Override
  public void init() throws ServletException {
    LogFactory.init();

    String name = getServletName();
    ZimbraLog.soap.info("Servlet " + name + " starting up");
    super.init();
  }

  @Override
  public void destroy() {
    String name = getServletName();
    ZimbraLog.soap.info("Servlet " + name + " shutting down");
    super.destroy();
  }

  @Override
  protected void doGet(
      javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp)
      throws javax.servlet.ServletException, IOException {
    ZimbraLog.clearContext();
    try {
      addRemoteIpToLoggingContext(req);
      String pathInfo = req.getPathInfo();
      if (pathInfo == null || pathInfo.length() == 0) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      if (pathInfo.startsWith("/")) {
        pathInfo = pathInfo.substring(1);
      }
      ZimbraLog.soap.debug("WSDL SERVLET Received a GET pathInfo=" + pathInfo);
      if (pathInfo.matches("^[a-zA-Z]+\\.xsd$")) {
        InputStream is = JaxbUtil.class.getResourceAsStream(pathInfo);
        if (is == null) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        resp.setContentType(MimeConstants.CT_TEXT_XML);
        ByteUtil.copy(is, true /* closeIn */, resp.getOutputStream(), false /* closeOut */);
      } else {
        Domain domain = getBestDomain(req.getServerName());
        if (!WsdlGenerator.handleRequestForWsdl(
            pathInfo, resp.getOutputStream(), getSoapURL(domain), getSoapAdminURL(domain))) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
      }
    } finally {
      ZimbraLog.clearContext();
    }
  }

  private Domain getBestDomain(String serverName) {
    Provisioning prov = Provisioning.getInstance();
    Domain domain;
    try {
      domain = prov.getDomainByName(serverName);
      if (domain != null) {
        return domain;
      }
    } catch (ServiceException e) {
    }
    try {
      Server server = prov.getLocalServer();
      if (server != null) {
        domain = prov.getDomainByName(serverName);
        if (domain != null) {
          return domain;
        }
      }
    } catch (ServiceException e) {
    }
    try {
      return prov.getDefaultDomain();
    } catch (ServiceException e) {
      return null;
    }
  }

  private static String getSoapAdminURL(Domain domain) {
    try {
      if (LC.wsdl_use_public_service_hostname.booleanValue()) {
        return URLUtil.getPublicAdminSoapURLForDomain(
            Provisioning.getInstance().getLocalServer(), domain);
      }
      return URLUtil.getAdminURL(Provisioning.getInstance().getLocalServer());
    } catch (ServiceException e) {
      return WsdlServiceInfo.localhostSoapAdminHttpsURL;
    }
  }

  private static String getSoapURL(Domain domain) {
    try {
      if (LC.wsdl_use_public_service_hostname.booleanValue()) {
        return URLUtil.getPublicURLForDomain(
            Provisioning.getInstance().getLocalServer(),
            domain,
            AccountConstants.USER_SERVICE_URI,
            true);
      }
      return URLUtil.getServiceURL(
          Provisioning.getInstance().getLocalServer(), AccountConstants.USER_SERVICE_URI, true);
    } catch (ServiceException e) {
      return WsdlServiceInfo.localhostSoapHttpURL;
    }
  }
}
