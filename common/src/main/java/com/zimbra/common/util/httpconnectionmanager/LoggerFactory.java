package com.zimbra.common.util.httpconnectionmanager;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;

class LoggerFactory {

  private LoggerFactory() {
    // Utility class
  }
  private static final Log sLog = LogFactory.getLog(ZimbraHttpConnectionManager.class);

  public static Log getDefaultLogger() {
    return sLog;
  }
}
