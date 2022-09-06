// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.util.List;
import java.util.Map;

public class MailHostPool extends AttributeCallback {

  /** check to make sure zimbraMailHostPool points to a valid server id */
  @Override
  public void preModify(
      CallbackContext context, String attrName, Object value, Map attrsToModify, Entry entry)
      throws ServiceException {

    MultiValueMod mod = multiValueMod(attrsToModify, Provisioning.A_zimbraMailHostPool);

    if (mod.adding() || mod.replacing()) {
      Provisioning prov = Provisioning.getInstance();
      List<String> pool = mod.values();
      for (String host : pool) {
        if (host == null || host.equals("")) continue;
        Server server = prov.get(Key.ServerBy.id, host);
        if (server == null)
          throw ServiceException.INVALID_REQUEST(
              "specified "
                  + Provisioning.A_zimbraMailHostPool
                  + " does not correspond to a valid server: "
                  + host,
              null);
        else {
          if (!server.hasMailClientService()) {
            throw ServiceException.INVALID_REQUEST(
                "specified "
                    + Provisioning.A_zimbraMailHost
                    + " is not a mailclient server with service webapp enabled: "
                    + host,
                null);
          }
        }
      }
    }
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {}
}
