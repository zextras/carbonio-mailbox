// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.AutoProvisionThread;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.util.Zimbra;
import java.util.Map;

public class AutoProvPollingInterval extends AttributeCallback {

  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
      throws ServiceException {}

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {
    if (!Provisioning.A_zimbraAutoProvPollingInterval.equalsIgnoreCase(attrName)) {
      return;
    }

    // do not run this callback unless inside the server
    if (!Zimbra.started()) {
      return;
    }

    try {
      if (entry instanceof Server) {
        // sanity check, this should not happen because ModifyServer is
        // proxied to the the right server
        if (!((Server) entry).isLocalServer()) {
          return;
        }
      }
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("unable to validate server", e);
      return;
    }

    try {
      AutoProvisionThread.switchAutoProvThreadIfNecessary();
    } catch (ServiceException e) {
      ZimbraLog.autoprov.error("unable to switch auto provisioning thread", e);
    }
  }
}
