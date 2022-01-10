// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.httpclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;

public class HttpProxyUtil {

    private static String sProxyUrl = null;
    private static URI sProxyUri = null;
    private static AuthScope sProxyAuthScope = null;
    private static UsernamePasswordCredentials sProxyCreds = null;

    public static synchronized void configureProxy(HttpClientBuilder clientBuilder) {
        try {
            String url = Provisioning.getInstance().getLocalServer().getAttr(Provisioning.A_zimbraHttpProxyURL, null);
            if (url == null) return;

            // need to initializae all the statics
            if (sProxyUrl == null || !sProxyUrl.equals(url)) {
                sProxyUrl = url;
                sProxyUri = new URI(url);
                sProxyAuthScope = null;
                sProxyCreds = null;
                String userInfo = sProxyUri.getUserInfo();
                if (userInfo != null) {
                    int i = userInfo.indexOf(':');
                    if (i != -1) {
                        sProxyAuthScope = new AuthScope(sProxyUri.getHost(), sProxyUri.getPort(), null);
                        sProxyCreds = new UsernamePasswordCredentials(userInfo.substring(0, i), userInfo.substring(i+1));
                    }
                }
            }
            if (ZimbraLog.misc.isDebugEnabled()) {
                ZimbraLog.misc.debug("setting proxy: "+url);
            }

            HttpHost proxy = new HttpHost(sProxyUri.getHost(), sProxyUri.getPort());
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            clientBuilder.setDefaultRequestConfig(config);
            if (sProxyAuthScope != null && sProxyCreds != null)  {
                CredentialsProvider cred = new BasicCredentialsProvider();
                cred.setCredentials(sProxyAuthScope, sProxyCreds);
                clientBuilder.setDefaultCredentialsProvider(cred);
            }
        } catch (ServiceException e) {
            ZimbraLog.misc.warn("Unable to configureProxy: "+e.getMessage(), e);
        } catch (URISyntaxException e) {
            ZimbraLog.misc.warn("Unable to configureProxy: "+e.getMessage(), e);
        }
    }
}
