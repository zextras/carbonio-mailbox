// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.httpclient;

import java.io.IOException;
import org.apache.hc.core5.http.HttpRequest;
import java.net.URISyntaxException;
import java.net.URI;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.HttpEntity;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.net.ProxyHostConfiguration;
import com.zimbra.common.util.ZimbraHttpConnectionManager;

public class HttpClientUtil {

    public static ClassicHttpResponse executeMethod(HttpUriRequestBase method) throws IOException {
        return executeMethod(ZimbraHttpConnectionManager.getInternalHttpConnMgr().getDefaultHttpClient().build(), method, null);
    }
    public static ClassicHttpResponse executeMethod(HttpClient client, HttpUriRequestBase method) throws IOException {
        return executeMethod(client, method, null, null);
    }

    public static ClassicHttpResponse executeMethod(HttpClient client, HttpUriRequestBase method, BasicCookieStore state, HttpClientContext context) throws IOException {
        ProxyHostConfiguration proxyConfig = HttpProxyConfig.getProxyConfig(HttpClientUtil.requestUri(method).toString());
        return executeWithProxy(client, method, state, proxyConfig);
    }

    static ClassicHttpResponse executeWithProxy(HttpClient client, HttpUriRequestBase method, BasicCookieStore state, ProxyHostConfiguration proxyConfig) throws IOException {
        if (proxyConfig == null || proxyConfig.getUsername() == null || proxyConfig.getPassword() == null) {
            return client.executeOpen(null, method, null);
        }
        HttpHost proxy = new HttpHost(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
        CredentialsStore credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy),
            new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword().toCharArray()));
        HttpClientContext proxyContext = HttpClientContext.create();
        proxyContext.setRequestConfig(RequestConfig.custom().setProxy(proxy).build());
        proxyContext.setCredentialsProvider(credsProvider);
        proxyContext.setCookieStore(state != null ? state : new BasicCookieStore());
        return client.executeOpen(null, method, proxyContext);
    }

    public static ClassicHttpResponse executeMethod(HttpClientBuilder clientBuilder, HttpUriRequestBase method, BasicCookieStore state, HttpClientContext context) throws IOException {
        if (state != null) {
            clientBuilder.setDefaultCookieStore(state);
        }
        return executeMethod(clientBuilder.build(), method, state, context);
    }

    public static ClassicHttpResponse executeMethod(HttpClient client, HttpUriRequestBase method, HttpClientContext context) throws IOException {
        return client.executeOpen(null, method, context);
    }

    public static BasicCookieStore newHttpState(ZAuthToken authToken, String host, boolean isAdmin) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        if (authToken != null) {
            Map<String, String> cookieMap = authToken.cookieMap(isAdmin);
            if (cookieMap != null) {
                for (Map.Entry<String, String> ck : cookieMap.entrySet()) {
                    BasicClientCookie cookie = new BasicClientCookie(ck.getKey(), ck.getValue());
                    cookie.setDomain(host);
                    cookie.setPath("/");
                    cookie.setSecure(false);
                    cookieStore.addCookie(cookie);
                }
            }
        }
        return cookieStore;
    }

    /**
     * Returns cookie store for Apache HTTP Components HttpClient 4.x
     * @param authToken
     * @param host
     * @param isAdmin
     * @return {@link CookieStore}
     */
    public static CookieStore newCookieStore(ZAuthToken authToken, String host, boolean isAdmin) {
        CookieStore cookieStore = new BasicCookieStore();
        if (authToken != null) {
            Map<String, String> cookieMap = authToken.cookieMap(isAdmin);
            if (cookieMap != null) {
                for (Map.Entry<String, String> ck : cookieMap.entrySet()) {
                    BasicClientCookie cookie = new BasicClientCookie(ck.getKey(), ck.getValue());
                    cookie.setDomain(host);
                    cookie.setPath("/");
                    cookie.setSecure(false);
                    cookieStore.addCookie(cookie);
                }
            }
        }
        return cookieStore;
    }

    public static String entityToString(HttpEntity entity) throws IOException {
        try {
            return EntityUtils.toString(entity);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String entityToString(HttpEntity entity, String charset) throws IOException {
        try {
            return EntityUtils.toString(entity, charset);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String entityToString(HttpEntity entity, Charset charset) throws IOException {
        try {
            return EntityUtils.toString(entity, charset);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static URI requestUri(HttpRequest request) {
        try {
            return request.getUri();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
