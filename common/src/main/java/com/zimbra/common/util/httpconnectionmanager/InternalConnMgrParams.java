package com.zimbra.common.util.httpconnectionmanager;

import com.zimbra.common.localconfig.LC;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;

class InternalConnMgrParams extends ZimbraConnMgrParams {

  public InternalConnMgrParams() {
    this.socketConfig = SocketConfig.custom().setSoTimeout(LC.httpclient_external_connmgr_so_timeout.intValue())
        .setTcpNoDelay(LC.httpclient_external_connmgr_tcp_nodelay.booleanValue()).build();
    this.requestConfig = RequestConfig.custom()
        .setStaleConnectionCheckEnabled(LC.httpclient_external_connmgr_stale_connection_check.booleanValue())
        .setConnectTimeout(LC.httpclient_internal_connmgr_connection_timeout.intValue())
        .build();
  }

  @Override
  public long getHttpClientConnectionTimeout() {
    return LC.httpclient_internal_client_connection_timeout.longValue();
  }

  @Override
  long getReaperSleepInterval() {
    return LC.httpclient_internal_connmgr_idle_reaper_sleep_interval.longValue();
  }

  @Override
  long getReaperConnectionTimeout() {
    return LC.httpclient_internal_connmgr_idle_reaper_connection_timeout.longValue();
  }

  @Override
  int getDefaultMaxConnectionsPerHost() {
    return LC.httpclient_internal_connmgr_max_host_connections.intValue();
  }

  @Override
  int getMaxTotalConnection() {
    return LC.httpclient_internal_connmgr_max_total_connections.intValue();
  }

}
