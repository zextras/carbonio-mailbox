// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.httpclient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.ssl.SSLContexts;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.CustomTrustManager;
import com.zimbra.common.util.ZimbraLog;

/**
 * 
 * @author Greg Solovyev
 *
 * Wrapper class for Apache HTTP Components HttpClient 4. 
 * Server code should use this wrapper to ensure that outgoing HTTP connections are being reused. 
 */

public class ZimbraHttpClientManager {
    protected static ZimbraHttpClientManager instance;
    private final CloseableHttpAsyncClient internalAsyncClient;
    private final CloseableHttpClient internalClient;
    private final CloseableHttpClient externalClient;
    public ZimbraHttpClientManager() {
        PoolingHttpClientConnectionManager internalConnectionMgr;
        PoolingHttpClientConnectionManager externallConnectionMgr;
        RequestConfig internalRequestConfig;
        RequestConfig externalRequestConfig;
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(CustomTrustManager.loadKeyStore(), new TrustSelfSignedStrategy()).build();
        } catch (GeneralSecurityException e) {
            ZimbraLog.security.error("Failed to load local keystore");
        }
        PoolingAsyncClientConnectionManagerBuilder asyncConnectionMgr = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(LC.httpclient_internal_connmgr_max_total_connections.intValue());
        if(sslcontext != null) {
            asyncConnectionMgr.setTlsStrategy(ClientTlsStrategyBuilder.create().setSslContext(sslcontext).build());
        }
        internalAsyncClient = HttpAsyncClients.custom().setConnectionManager(asyncConnectionMgr.build()).build();

        internalAsyncClient.start();

        internalConnectionMgr = new PoolingHttpClientConnectionManager();
        internalConnectionMgr.setDefaultMaxPerRoute(LC.httpclient_internal_connmgr_max_host_connections.intValue());
        internalConnectionMgr.setMaxTotal(LC.httpclient_internal_connmgr_max_total_connections.intValue());
        internalConnectionMgr.setDefaultConnectionConfig(staleCheck(LC.httpclient_internal_connmgr_stale_connection_check.booleanValue()));
        internalRequestConfig = RequestConfig.custom()
                .setConnectTimeout(LC.httpclient_internal_connmgr_connection_timeout.intValue(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(LC.httpclient_internal_connmgr_so_timeout.intValue(), TimeUnit.MILLISECONDS)
                    .build();
        internalClient = HttpClientBuilder.create()
                .setConnectionManager(internalConnectionMgr)
                .setDefaultRequestConfig(internalRequestConfig)
                            .build();
        externallConnectionMgr = new PoolingHttpClientConnectionManager();
        externallConnectionMgr.setDefaultMaxPerRoute(LC.httpclient_external_connmgr_max_host_connections.intValue());
        externallConnectionMgr.setMaxTotal(LC.httpclient_external_connmgr_max_total_connections.intValue());
        externallConnectionMgr.setDefaultConnectionConfig(staleCheck(LC.httpclient_external_connmgr_stale_connection_check.booleanValue()));
        externalRequestConfig = RequestConfig.custom().
                setConnectTimeout(LC.httpclient_external_connmgr_connection_timeout.intValue(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(LC.httpclient_external_connmgr_so_timeout.intValue(), TimeUnit.MILLISECONDS)
                    .build();
        externalClient = HttpClientBuilder.create()
                .setConnectionManager(externallConnectionMgr)
                .setDefaultRequestConfig(externalRequestConfig)
                                .build();
    }

    private static ConnectionConfig staleCheck(boolean enabled) {
        return ConnectionConfig.custom()
                .setValidateAfterInactivity(enabled ? TimeValue.ZERO_MILLISECONDS : TimeValue.NEG_ONE_MILLISECOND)
                .build();
    }

    public static synchronized ZimbraHttpClientManager getInstance() {
        if(instance == null) {
            instance = new ZimbraHttpClientManager();
        }
        return instance;
    }

    /**
     * @return instance of asynchronous HttpClient to be used for connections between Zimbra services
     */
    public CloseableHttpAsyncClient getInternalAsyncHttpClient() {
        return internalAsyncClient;
    }

    /**
     * @return instance of asynchronous HttpClient to be used for connections between Zimbra services
     */
    public CloseableHttpClient getInternalHttpClient() {
        return internalClient;
    }

    public CloseableHttpClient getExternalHttpClient() {
        return externalClient;
    }

    /**
     * orderly shutdown the client
     */
    @PreDestroy
    public void shutDown() throws IOException {
        internalAsyncClient.close();
        internalClient.close();
        externalClient.close();
    }
}
