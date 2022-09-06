// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.oauth;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.oauth.utils.OAuthServiceProvider;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;

/**
 * Authorization request handler for OAuth.
 *
 * @author pgajjar
 */
public class OAuthAuthorizationServlet extends ZimbraServlet {

  private static final Log LOG = ZimbraLog.oauth;

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    LOG.debug("Authorization Handler doGet requested!");
    try {
      OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);
      OAuthAccessor accessor = OAuthServiceProvider.getAccessor(oAuthMessage);

      if (Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
        // already authorized send the user back
        returnToConsumer(request, response, accessor);
      } else {
        sendToAuthorizePage(request, response, accessor);
      }
    } catch (Exception e) {
      OAuthServiceProvider.handleException(e, request, response, true);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    LOG.debug("Authorization Handler doPost requested!");

    try {
      OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
      OAuthAccessor accessor = OAuthServiceProvider.getAccessor(requestMessage);

      // status can be yes/no(accept/declined)
      String status = (String) request.getAttribute("STATUS");

      if (null != status && status.equals("no")) {
        LOG.debug("Access to zimbra message is denied.");
        OAuthTokenCache.remove(accessor.requestToken, OAuthTokenCache.REQUEST_TOKEN_TYPE);
        sendUnauthorizedResponse(response, accessor);
        return;
      }

      String username = request.getParameter("username");
      String zmtoken = (String) request.getAttribute("ZM_AUTH_TOKEN");

      LOG.debug(
          "[AuthorizationHandlerInput] username = %s, oauth_token = %s, ZM_AUTH_TOKEN = %s",
          username, request.getParameter("oauth_token"), zmtoken);

      if (zmtoken == null) {
        sendToAuthorizePage(request, response, accessor);
      } else {
        OAuthServiceProvider.markAsAuthorized(accessor, request.getParameter("username"), zmtoken);
        OAuthServiceProvider.generateVerifier(accessor);
        returnToConsumer(request, response, accessor);
      }
    } catch (Exception e) {
      LOG.debug("AuthorizationHandler exception", e);
      OAuthServiceProvider.handleException(e, request, response, true);
    }
  }

  private void sendToAuthorizePage(
      HttpServletRequest request, HttpServletResponse response, OAuthAccessor accessor)
      throws IOException, ServletException {
    String consumer_app_name = (String) accessor.consumer.getProperty("app_name");

    LOG.debug(
        "[AuthorizationHandlerOutputToAuthorizePage] request token = %s, consumer-app = %s,"
            + " ZM_AUTH_TOKEN = %s",
        accessor.requestToken, consumer_app_name, request.getParameter("oauth_token"));

    request.setAttribute("CONS_APP_NAME", consumer_app_name);
    request.setAttribute("TOKEN", accessor.requestToken);

    RequestDispatcher dispatcher =
        getServletContext().getContext("/zimbra").getRequestDispatcher("/public/authorize.jsp");
    if (dispatcher != null) {
      dispatcher.forward(request, response);
      return;
    }
  }

  private void returnToConsumer(
      HttpServletRequest request, HttpServletResponse response, OAuthAccessor accessor)
      throws IOException, ServletException {
    // send the user back to site's callBackUrl
    String callback = (String) accessor.getProperty(OAuth.OAUTH_CALLBACK);

    if ("oob".equals(callback)) {
      // no call back it must be a client
      response.setContentType("text/plain");
      PrintWriter out = response.getWriter();
      out.println(
          "You have successfully authorized '"
              + accessor.consumer.getProperty("app_name")
              + "'. Your verification code is "
              + accessor.getProperty(OAuth.OAUTH_VERIFIER).toString()
              + ". Please close this browser window and click continue"
              + " in the client.");
      out.close();
    } else {
      String token = accessor.requestToken;
      String verifier = accessor.getProperty(OAuth.OAUTH_VERIFIER).toString();
      if (token != null) {
        callback =
            OAuth.addParameters(callback, "oauth_token", token, OAuth.OAUTH_VERIFIER, verifier);
      }

      callback =
          String.format(
              "%s&zimbra_cn=%s&zimbra_givenname=%s&zimbra_sn=%s&zimbra_email=%s&zimbra_displayname=%s",
              callback,
              accessor.getProperty("ZM_ACC_CN"),
              accessor.getProperty("ZM_ACC_GIVENNAME"),
              accessor.getProperty("ZM_ACC_SN"),
              accessor.getProperty("ZM_ACC_EMAIL"),
              accessor.getProperty("ZM_ACC_DISPLAYNAME"));

      LOG.debug("[AuthorizationHandlerRedirectURL]" + callback);

      response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
      response.setHeader("Location", callback);
      // not sending back ZM_AUTH_TOKEN to consumer
      response.setHeader("Set-Cookie", "");
    }
  }

  private void sendUnauthorizedResponse(HttpServletResponse response, OAuthAccessor accessor)
      throws IOException {
    String callback = (String) accessor.getProperty(OAuth.OAUTH_CALLBACK);
    callback += "?authorized=false";
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.sendRedirect(callback);
  }

  private static final long serialVersionUID = 6775946952939185091L;
}
