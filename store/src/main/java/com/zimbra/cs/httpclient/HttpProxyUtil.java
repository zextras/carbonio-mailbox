// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.httpclient;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpProxyUtil {

  private HttpProxyUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static synchronized void configureProxy(HttpClientBuilder clientBuilder) {
    try {
      final var httpProxyUrl = Provisioning.getInstance().getLocalServer().getAttr(ZAttrProvisioning.A_zimbraHttpProxyURL, null);
      if (httpProxyUrl == null || httpProxyUrl.isEmpty()) {
        ZimbraLog.misc.info("HttpProxyUtil.configureProxy 'zimbraHttpProxyURL' is null or empty, not using proxy.");
        return;
      }

      var uri = new URI(httpProxyUrl);
      var proxyHost = uri.getHost();
      var proxyPort = uri.getPort();

      var userInfo = uri.getUserInfo();
      String username = null;
      String password = null;
      if (userInfo != null) {
        var credentials = userInfo.split(":");
        if (credentials.length == 2) {
          username = credentials[0];
          password = credentials[1];
        }
      }

      if (username != null && password != null) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new AuthScope(proxyHost, proxyPort),
            new UsernamePasswordCredentials(username, password)
        );
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }

      var proxy = new HttpHost(proxyHost, proxyPort);
      var config = RequestConfig.custom()
          .setProxy(proxy)
          .build();
      clientBuilder.setDefaultRequestConfig(config);

      if (ZimbraLog.misc.isDebugEnabled()) {
        ZimbraLog.misc.debug("setting proxy: " + httpProxyUrl);
      }

    } catch (ServiceException | URISyntaxException e) {
      ZimbraLog.misc.warn("Unable to configureProxy: " + e.getMessage(), e);
    }
  }
}
