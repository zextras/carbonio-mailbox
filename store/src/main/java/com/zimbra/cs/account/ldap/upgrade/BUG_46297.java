// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
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

public class BUG_46297 extends UpgradeOp {

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

    String attrName = Provisioning.A_zimbraContactHiddenAttributes;

    printer.println();
    printer.println("Checking " + entryName);

    String oldValue =
        "dn,zimbraAccountCalendarUserType,zimbraCalResType,zimbraCalResLocationDisplayName,zimbraCalResCapacity,zimbraCalResContactEmail";
    String newValue =
        "dn,zimbraAccountCalendarUserType,zimbraCalResType,zimbraCalResLocationDisplayName,zimbraCalResCapacity,zimbraCalResContactEmail,vcardUID,vcardURL,vcardXProps";

    String curValue = entry.getAttr(attrName);

    boolean needsUpdate =
        curValue == null || (oldValue.equals(curValue) && !newValue.equals(curValue));

    if (needsUpdate) {
      printer.println(
          "    Modifying "
              + attrName
              + " on "
              + entryName
              + " from ["
              + curValue
              + "] to ["
              + newValue
              + "]");

      Map<String, Object> attr = new HashMap<String, Object>();
      attr.put(attrName, newValue);
      prov.modifyAttrs(entry, attr);
    } else {
      printer.println(
          "    "
              + attrName
              + " already has an effective value: ["
              + curValue
              + "] on entry "
              + entryName
              + " - skipping");
    }
  }

  private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
    Config config = prov.getConfig();
    doEntry(zlc, config, "global config");
  }

  private void doAllServers(ZLdapContext zlc) throws ServiceException {
    List<Server> servers = prov.getAllServers();

    for (Server server : servers) {
      doEntry(zlc, server, "server " + server.getName());
    }
  }
}
