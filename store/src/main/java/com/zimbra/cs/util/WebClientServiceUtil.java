// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.zimlet.ZimletUtil;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Util class for sending service related requests from service node to ui nodes
 */
public class WebClientServiceUtil {

  public static final String PARAM_AUTHTOKEN = "authtoken";
  private static final String FLUSH_UISTRINGS_ON_UI_NODE = "/fromservice/flushuistrings";

  public static String sendServiceRequestToUiNode(Server server, String serviceUrl)
      throws ServiceException {
    if (isServerAtLeast8dot5(server)) {
      HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getExternalHttpConnMgr()
          .newHttpClient();
      HttpProxyUtil.configureProxy(clientBuilder);
      AuthToken authToken = AuthProvider.getAdminAuthToken();
      ZimbraLog.misc.debug("got admin auth token");
      String resp = "";
      HttpRequestBase method = null;
      try {
        method = new HttpGet(URLUtil.getServiceURL(server, serviceUrl, false));
        ZimbraLog.misc.debug("connecting to ui node %s", server.getName());
        method.addHeader(PARAM_AUTHTOKEN, authToken.getEncoded());
        HttpResponse httpResp = HttpClientUtil.executeMethod(clientBuilder.build(), method);
        int result = httpResp.getStatusLine().getStatusCode();
        ZimbraLog.misc.debug("resp: %d", result);
        resp = EntityUtils.toString(httpResp.getEntity());
        ZimbraLog.misc.debug("got response from ui node: %s", resp);
      } catch (IOException | HttpException e) {
        ZimbraLog.misc.warn("failed to get response from ui node", e);
      } catch (AuthTokenException e) {
        ZimbraLog.misc.warn("failed to get authToken", e);
      } finally {
        if (method != null) {
          method.releaseConnection();
        }
      }
      if (authToken != null && authToken.isRegistered()) {
        try {
          authToken.deRegister();
          ZimbraLog.misc.debug("de-registered auth token, isRegistered?%s",
              authToken.isRegistered());
        } catch (AuthTokenException e) {
          ZimbraLog.misc.warn("failed to de-register authToken", e);
        }
      }
      return resp;
    }
    return "";
  }

  public static void sendFlushZimletRequestToUiNode(Server server) throws ServiceException {
    sendServiceRequestToUiNode(server, "/fromservice/flushzimlets");
  }

  private static void postToUiNode(Server server, HttpPost method, String authToken)
      throws ServiceException {
    HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getExternalHttpConnMgr()
        .newHttpClient();
    HttpProxyUtil.configureProxy(clientBuilder);
    try {
      method.addHeader(PARAM_AUTHTOKEN, authToken);
      ZimbraLog.zimlet.debug("connecting to ui node %s", server.getName());
      HttpResponse httpResp = HttpClientUtil.executeMethod(clientBuilder.build(), method);
      int respCode = httpResp.getStatusLine().getStatusCode();
      if (respCode != 200) {
        ZimbraLog.zimlet.warn("operation failed, return code: %d", respCode);
      }
    } catch (Exception e) {
      ZimbraLog.zimlet.warn("operation failed for node %s", server.getName(), e);
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
  }

  public static void sendDeployZimletRequestToUiNode(Server server, String zimlet, String authToken,
      byte[] data)
      throws ServiceException {
    if (isServerAtLeast8dot5(server)) {
      HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getExternalHttpConnMgr()
          .newHttpClient();
      HttpProxyUtil.configureProxy(clientBuilder);
      HttpPost method = new HttpPost(
          URLUtil.getServiceURL(server, "/fromservice/deployzimlet", false));
      method.addHeader(ZimletUtil.PARAM_ZIMLET, zimlet);
      ZimbraLog.zimlet.info("connecting to ui node %s, data size %d", server.getName(),
          data.length);

      method.setEntity(new ByteArrayEntity(data));
      postToUiNode(server, method, authToken);
    }
  }

  private static boolean isServerAtLeast8dot5(Server server) {
    return true;
  }

  public static void sendUndeployZimletRequestToUiNode(Server server, String zimlet,
      String authToken)
      throws ServiceException {
    if (isServerAtLeast8dot5(server)) {
      HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getExternalHttpConnMgr()
          .newHttpClient();
      HttpProxyUtil.configureProxy(clientBuilder);
      HttpPost method = new HttpPost(
          URLUtil.getServiceURL(server, "/fromservice/undeployzimlet", false));
      method.addHeader(ZimletUtil.PARAM_ZIMLET, zimlet);
      postToUiNode(server, method, authToken);
    }
  }
}
