// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.oauth;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.oauth.utils.OAuthServiceProvider;
import com.zimbra.cs.servlet.ZimbraServlet;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet to handle OAuth request-token request(/service/oauth/req-token)
 *
 * @author Yutaka Obuchi
 */
public class OAuthRequestTokenServlet extends ZimbraServlet {

    private static final Log LOG = ZimbraLog.oauth;

    public void init() throws ServletException {
        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LOG.debug("RequestTokenHandler doGet requested!");
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LOG.debug("RequestTokenHandler doPost requested!");
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);

            OAuthConsumer consumer = OAuthServiceProvider.getConsumer(oAuthMessage);

            //TODO: This property is applicable for mobile client.
            //For other types of consumers there will be more fields(e.g. Browser)
            String device = request.getParameter("device");
            consumer.setProperty("device", device);

            OAuthAccessor accessor = new OAuthAccessor(consumer);
            OAuthServiceProvider.VALIDATOR.validateReqTokenMessage(oAuthMessage, accessor);

            // generate request_token and secret
            OAuthServiceProvider.generateRequestToken(accessor);

            response.setContentType("text/plain");
            OutputStream out = response.getOutputStream();
            OAuth.formEncode(OAuth.newList("oauth_token", accessor.requestToken,
                    "oauth_token_secret", accessor.tokenSecret,
                    OAuth.OAUTH_CALLBACK_CONFIRMED, "true"), out);
            out.close();
        } catch (Exception e){
            LOG.debug("RequestTokenHandler exception", e);
            OAuthServiceProvider.handleException(e, request, response, true);
        }
    }

    private static final long serialVersionUID = 8073596020574048845L;

}