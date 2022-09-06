// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Oct 26, 2005
 *
 */
package com.zimbra.cs.extension;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.service.admin.AdminAccessControl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP handler for an extension. HTTP GET and POST requests are dispatched to the handler for
 * processing. Each extension can define multiple handlers and register them under the extension
 * with different paths.
 *
 * @author kchen
 */
public abstract class ExtensionHttpHandler {

  protected ZimbraExtension mExtension;

  /**
   * The path under which the handler is registered for an extension.
   *
   * @return
   */
  public String getPath() {
    return "/" + mExtension.getName();
  }

  /**
   * Processes HTTP OPTIONS requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP OPTIONS requests are not supported");
  }

  /**
   * Processes HTTP GET requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP GET requests are not supported");
  }

  /**
   * Processes HTTP POST requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP POST requests are not supported");
  }

  /**
   * Processes HTTP PUT requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP PUT requests are not supported");
  }

  /**
   * Processes HTTP DELETE requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP DELETE requests are not supported");
  }

  /**
   * Processes HTTP PATCH requests.
   *
   * @param req
   * @param resp
   * @throws IOException
   * @throws ServletException
   */
  public void doPatch(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    throw new ServletException("HTTP PATCH requests are not supported");
  }

  /**
   * Called to initialize the handler. If initialization fails, the handler is not registered.
   *
   * @param ext the extension to which this handler belongs
   * @throws ServiceException
   */
  public void init(ZimbraExtension ext) throws ServiceException {
    mExtension = ext;
  }

  /** Called to terminate the handler. */
  public void destroy() {}

  /** Hides the extension for requests sent to the default mail port and mail SSL port. */
  public boolean hideFromDefaultPorts() {
    return false;
  }

  /**
   * This API is for checking ACL rights for REST handlers that are added through server extensions.
   *
   * @param authToken
   * @param target
   * @param needed
   * @return
   * @throws ServiceException
   */
  protected static AdminAccessControl checkRight(AuthToken authToken, Entry target, Object needed)
      throws ServiceException {
    AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(authToken);
    aac.checkRight(target, needed);
    return aac;
  }
}
