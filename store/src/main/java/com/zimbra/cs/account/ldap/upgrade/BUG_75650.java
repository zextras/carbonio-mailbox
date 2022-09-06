// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.HashMap;
import java.util.Map;

public class BUG_75650 extends UpgradeOp {

  private static final String ATTR_NAME = Provisioning.A_zimbraMailPurgeBatchSize;
  private static final int OLD_VALUE = 10000;
  private static final int NEW_VALUE = 1000;

  @Override
  void doUpgrade() throws ServiceException {
    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doGlobalConfig(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
    doEntry(zlc, prov.getConfig());
  }

  private void doEntry(ZLdapContext zlc, Entry entry) throws ServiceException {
    String entryName = entry.getLabel();

    printer.println();
    printer.println("------------------------------");
    printer.println("Checking " + ATTR_NAME + " on " + entryName);

    int curValue = entry.getIntAttr(ATTR_NAME, NEW_VALUE);
    if (curValue == OLD_VALUE) {
      Map<String, Object> attrs = new HashMap<String, Object>();
      printer.println(
          "Changing " + ATTR_NAME + " on " + entryName + " from " + OLD_VALUE + " to " + NEW_VALUE);
      attrs.put(Provisioning.A_zimbraMailPurgeBatchSize, NEW_VALUE);
      modifyAttrs(entry, attrs);
    } else {
      printer.println(
          "Current value of "
              + ATTR_NAME
              + " on "
              + entryName
              + " is "
              + curValue
              + " - not changed");
    }
  }
}
