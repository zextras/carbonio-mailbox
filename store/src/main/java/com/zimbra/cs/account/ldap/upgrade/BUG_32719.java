// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BUG_32719 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doGlobalConfig(zlc);
      doAllServers(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  private void doEntry(ZLdapContext zlc, Entry entry, String entryName) throws ServiceException {

    String oldAttr = Provisioning.A_zimbraHsmAge;
    String newAttr = Provisioning.A_zimbraHsmPolicy;

    printer.println();
    printer.println("Checking " + entryName);

    String oldValue = entry.getAttr(oldAttr, false);
    String newValue = entry.getAttr(newAttr, false);
    if (oldValue != null) {
      if (newValue == null) {
        newValue =
            String.format(
                "message,document:before:-%dminutes",
                entry.getTimeInterval(oldAttr, 0) / Constants.MILLIS_PER_MINUTE);

        printer.println(
            "    Setting "
                + newAttr
                + " on "
                + entryName
                + " from "
                + oldAttr
                + " value: ["
                + oldValue
                + "]"
                + " to ["
                + newValue
                + "]");

        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put(newAttr, newValue);
        prov.modifyAttrs(entry, attr);
      } else
        printer.println("    " + newAttr + " already has a value: [" + newValue + "], skipping");
    }
  }

  private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
    Config config = prov.getConfig();
    doEntry(zlc, config, "global config");
  }

  private void doAllServers(ZLdapContext zlc) throws ServiceException {
    List<Server> servers = prov.getAllServers();

    for (Server server : servers) doEntry(zlc, server, "server " + server.getName());
  }
}
