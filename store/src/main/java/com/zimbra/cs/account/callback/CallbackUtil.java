// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.util.Zimbra;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallbackUtil {

  public static Server verificationBeforeStartingThread(
      String expectedAttr, String currentAttr, Entry entry, String operation) {
    // if any other then expected attribute use this as a callback, return null
    if (!expectedAttr.equals(currentAttr)) {
      ZimbraLog.misc.debug("%s trying to invoke %s - not allowed", currentAttr, operation);
      return null;
    }

    // if zimbra is not started, return null
    if (!Zimbra.started()) {
      ZimbraLog.misc.debug("Zimbra not started yet");
      return null;
    }

    Server localServer = null;
    try {
      localServer = Provisioning.getInstance().getLocalServer();
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("%s : unable to get local server", operation);
      return null;
    }

    boolean hasMailboxService =
        localServer.getMultiAttrSet(Provisioning.A_zimbraServiceEnabled).contains("mailbox");
    if (!hasMailboxService) {
      ZimbraLog.misc.warn("%s : %s do not have mailbox services", operation, localServer.getName());
      return null;
    }

    if (entry instanceof Server) {
      Server server = (Server) entry;
      // sanity check, this should not happen because modifyServer is
      // proxied to the the right server
      if (server.getId() != localServer.getId()) {
        ZimbraLog.misc.warn("%s : wrong server", operation);
        return null;
      }
    }
    return localServer;
  }

  public static long getTimeInterval(String attrName, long prevValue) {
    long returnValue = 0;
    try {
      Server server = Provisioning.getInstance().getLocalServer();
      returnValue = server.getTimeInterval(attrName, prevValue);
    } catch (ServiceException e) {
      ZimbraLog.misc.warn(
          "Unable to determine value of %s. Using previous value: %d.", attrName, returnValue, e);
    }
    return returnValue;
  }

  private static String getServerAttrValue(String attrName) throws ServiceException {
    return Provisioning.getInstance().getLocalServer().getAttr(attrName, null);
  }

  public static boolean logStartup(String attrName) {
    try {
      String displayInterval = getServerAttrValue(attrName);
      ZimbraLog.misc.info("Starting %s thread with %s interval", attrName, displayInterval);
      return true;
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("Unable to get %s.  Aborting thread startup.", attrName, e);
      return false;
    }
  }

  public static List<Integer> getSortedMailboxIdList() throws ServiceException {
    int[] arr = MailboxManager.getInstance().getMailboxIds();
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < arr.length; i++) {
      list.add(arr[i]);
    }
    Collections.sort(list);
    return list;
  }

  public static boolean isLocalServer(Server server) throws ServiceException {
    Server local = Provisioning.getInstance().getLocalServer();
    return server.getId().equals(local.getId());
  }
}
