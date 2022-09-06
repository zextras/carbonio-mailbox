// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.localconfig.LocalConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.io.IOException;
import org.dom4j.DocumentException;

public class MailPort extends CheckPortConflict {

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {
    super.postModify(context, attrName, entry);
    if (entry instanceof Server) {
      Server localServer = null;
      try {
        localServer = Provisioning.getInstance().getLocalServer();
        if (entry == localServer) {
          String port = localServer.getAttr(attrName);
          LocalConfig lc = new LocalConfig(null);
          lc.set(LC.zimbra_mail_service_port.key(), port);
          lc.save();
        }
      } catch (ServiceException
          | DocumentException
          | ConfigException
          | NumberFormatException
          | IOException e) {
        ZimbraLog.misc.warn("Unable to update LC.zimbra_mail_port due to Exception", e);
      }
    }
  }
}
