// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Removes all account loggers and reloads {@code /opt/zextras/conf/log4j.properties}.
 *
 * @author ysasaki
 */
public final class ResetAllLoggers extends AdminDocumentHandler {

  private final Lock lock;

  public ResetAllLoggers() {
    this.lock = new ReentrantLock();
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    try {
      lock.lock();
      ZimbraLog.soap.info("Resetting all loggers");
      for (Log log : LogManager.getAllLoggers()) {
        log.removeAccountLoggers();
      }
    } finally {
      lock.unlock();
    }
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(AdminConstants.RESET_ALL_LOGGERS_RESPONSE);
    return response;
  }
}
