// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util.httpconnectionmanager;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class ZimbraHttpConnectionManager {

  private static final ZimbraHttpConnectionManager INTERNAL_CONN_MGR =
      new ZimbraHttpConnectionManager("Internal http client connection manager", new InternalConnMgrParams());

  private static final ZimbraHttpConnectionManager EXTERNAL_CONN_MGR =
      new ZimbraHttpConnectionManager("External http client connection manager", new ExternalConnMgrParams());
  private final String name;
  private final ZimbraConnMgrParams zimbraConnMgrParams;
  private final HttpClientBuilder httpClientBuilder;
  private final IdleConnectionReaper idleConnectionReaper;
  private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

  private ZimbraHttpConnectionManager(String name, ZimbraConnMgrParams zimbraConnMgrParams) {
    this.name = name;
    this.zimbraConnMgrParams = zimbraConnMgrParams;

    if (SocketFactories.getRegistry() != null) {
      this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(SocketFactories.getRegistry());
    } else {
      try {
        final SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (x509CertChain, authType) -> true).build();
        this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder
            .<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https",
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
            .build());
      } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
        LoggerFactory.getDefaultLogger().error("Error creating http connection manager with default socket factory", e);
      }
    }

    this.poolingHttpClientConnectionManager.setDefaultMaxPerRoute(
        zimbraConnMgrParams.getDefaultMaxConnectionsPerHost());
    this.poolingHttpClientConnectionManager.setMaxTotal(zimbraConnMgrParams.getMaxTotalConnection());

    this.poolingHttpClientConnectionManager.setDefaultSocketConfig(
        SocketConfig.custom().setSoTimeout(LC.socket_so_timeout.intValue()).build());

    this.httpClientBuilder = newHttpClientBuilder();

    this.idleConnectionReaper = new IdleConnectionReaper(this);
  }

  public static synchronized void startReaperThread() {
    INTERNAL_CONN_MGR.startIdleConnectionReaperThread();
    EXTERNAL_CONN_MGR.startIdleConnectionReaperThread();
  }

  public static synchronized void shutdownReaperThread() {
    INTERNAL_CONN_MGR.shutdownIdleConnectionReaperThread();
    EXTERNAL_CONN_MGR.shutdownIdleConnectionReaperThread();
  }

  public static ZimbraHttpConnectionManager getInternalHttpConnMgr() {
    return INTERNAL_CONN_MGR;
  }

  public static ZimbraHttpConnectionManager getExternalHttpConnMgr() {
    return EXTERNAL_CONN_MGR;
  }

  public ZimbraConnMgrParams getZimbraConnMgrParams() {
    return zimbraConnMgrParams;
  }

  public HttpClientBuilder getHttpClientBuilder() {
    return httpClientBuilder;
  }

  public void closeIdleConnections() {
    this.poolingHttpClientConnectionManager.closeIdleConnections(0, TimeUnit.MILLISECONDS);
  }

  public String getName() {
    return name;
  }

  public HttpClientConnectionManager getPoolingHttpClientConnectionManager() {
    return poolingHttpClientConnectionManager;
  }

  public HttpClientBuilder newHttpClientBuilder() {
    return HttpClients.custom()
        .setConnectionManager(this.poolingHttpClientConnectionManager)
        .setDefaultRequestConfig(this.zimbraConnMgrParams.requestConfig)
        .setDefaultSocketConfig(this.zimbraConnMgrParams.socketConfig);
  }

  private void startIdleConnectionReaperThread() {
    idleConnectionReaper.startReaperThread();
  }

  private void shutdownIdleConnectionReaperThread() {
    idleConnectionReaper.shutdownReaperThread();
  }

}