package com.zimbra.common.util.httpconnectionmanager;

import com.zimbra.common.util.Log;

class IdleConnectionReaper {

  private final ZimbraHttpConnectionManager zimbraHttpConnectionManager;

  private final Log logger = LoggerFactory.getDefaultLogger();

  private IdleConnectionMonitorThread idleConnectionMonitorThread;

  public IdleConnectionReaper(ZimbraHttpConnectionManager zimbraHttpConnectionManager) {
    this.zimbraHttpConnectionManager = zimbraHttpConnectionManager;
  }

  public void startReaperThread() {
    if (idleConnectionMonitorThread != null && idleConnectionMonitorThread.isAlive()) {
      logger.warn(
          "Cannot start a second http client IdleConnectionMonitorThread(Reaper thread) while another one is running.");
      return;
    }

    if (!isReaperEnabled()) {
      logger.info("Not starting http client IdleConnectionMonitorThread(Reaper thread) for "
          + zimbraHttpConnectionManager.getName() + " because it is disabled");
      return;
    }

    logger.info(
        "Starting http client IdleConnectionMonitorThread(Reaper thread) for " + zimbraHttpConnectionManager.getName()
            +
            " - reaper sleep interval=%d, reaper connection timeout=%d",
        getReaperSleepInterval(), getReaperConnectionTimeout());

    idleConnectionMonitorThread = new IdleConnectionMonitorThread(
        zimbraHttpConnectionManager.getPoolingHttpClientConnectionManager());
    idleConnectionMonitorThread.setName(
        "IdleConnectionTimeoutThread" + " for " + zimbraHttpConnectionManager.getName());
    idleConnectionMonitorThread.setConnectionTimeout(getReaperConnectionTimeout());
    idleConnectionMonitorThread.setTimeoutInterval(getReaperSleepInterval());
    idleConnectionMonitorThread.start();
  }

  public void shutdownReaperThread() {
    if (idleConnectionMonitorThread == null || !idleConnectionMonitorThread.isAlive()) {
      logger.warn(
          "requested shutting down http client IdleConnectionMonitorThread(Reaper thread) but the thread is not running");
      return;
    }

    logger.info("shutting down http client IdleConnectionMonitorThread(Reaper thread)");
    idleConnectionMonitorThread.shutdown();
    idleConnectionMonitorThread = null;
  }

  private boolean isReaperEnabled() {
    return zimbraHttpConnectionManager.getZimbraConnMgrParams().getReaperSleepInterval() != 0;
  }

  private long getReaperSleepInterval() {
    return zimbraHttpConnectionManager.getZimbraConnMgrParams().getReaperSleepInterval();
  }

  private long getReaperConnectionTimeout() {
    return zimbraHttpConnectionManager.getZimbraConnMgrParams().getReaperConnectionTimeout();
  }
}
