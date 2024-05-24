package com.zimbra.common.util.httpconnectionmanager;

import com.zimbra.common.util.Log;
import java.util.concurrent.TimeUnit;
import org.apache.http.conn.HttpClientConnectionManager;

class IdleConnectionMonitorThread extends Thread {

  private final HttpClientConnectionManager httpClientConnectionManager;

  private final Log logger = LoggerFactory.getDefaultLogger();

  private long timeoutInterval = 1000;
  private long connectionTimeout = 3000;
  private volatile boolean shutdown;

  public IdleConnectionMonitorThread(HttpClientConnectionManager httpClientConnectionManager) {
    super();
    this.httpClientConnectionManager = httpClientConnectionManager;
  }

  public void setTimeoutInterval(long timeoutInterval) {
    this.timeoutInterval = timeoutInterval;
  }

  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  @Override
  public void run() {
    try {
      while (!shutdown) {
        synchronized (this) {
          wait(this.timeoutInterval);
          httpClientConnectionManager.closeExpiredConnections();
          httpClientConnectionManager.closeIdleConnections(this.connectionTimeout, TimeUnit.MILLISECONDS);
        }
      }
    } catch (InterruptedException ex) {
      logger.debug("IdleConnectionMonitorThread(Reaper thread) was interrupted");
    }
  }

  public void shutdown() {
    shutdown = true;
    synchronized (this) {
      notifyAll();
    }
  }
}
