package com.zimbra.common.util.httpconnectionmanager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;

public abstract class ZimbraConnMgrParams {

  protected RequestConfig requestConfig = RequestConfig.DEFAULT;
  protected SocketConfig socketConfig = SocketConfig.DEFAULT;

  public RequestConfig getRequestConfig() {
    return requestConfig;
  }

  public SocketConfig getSocketConfig() {
    return socketConfig;
  }

  abstract long getHttpClientConnectionTimeout();

  abstract long getReaperSleepInterval();

  abstract long getReaperConnectionTimeout();

  abstract int getDefaultMaxConnectionsPerHost();

  abstract int getMaxTotalConnection();
}
