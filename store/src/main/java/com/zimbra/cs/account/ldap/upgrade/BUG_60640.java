// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BUG_60640 extends UpgradeOp {

  private static final String[] ATTR_NAMES =
      new String[] {
        Provisioning.A_zimbraPrefReadingPaneLocation,
        Provisioning.A_zimbraPrefTasksReadingPaneLocation
      };

  private static final String OLD_VALUE = "bottom";
  private static final String NEW_VALUE = "right";

  @Override
  void doUpgrade() throws ServiceException {
    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doAllCos(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  @Override
  Description getDescription() {
    return new Description(
        this,
        ATTR_NAMES,
        new EntryType[] {EntryType.COS},
        OLD_VALUE,
        NEW_VALUE,
        String.format(
            "Upgrade attribute %s on all cos from \"%s\" to \"%s\"",
            Arrays.deepToString(ATTR_NAMES), OLD_VALUE, NEW_VALUE));
  }

  private void doEntry(ZLdapContext zlc, Entry entry) throws ServiceException {
    String entryName = entry.getLabel();

    printer.println();
    printer.println("------------------------------");
    printer.format("Checking %s on cos %s\n", Arrays.deepToString(ATTR_NAMES), entryName);

    Map<String, Object> attrs = new HashMap<String, Object>();
    for (String attrName : ATTR_NAMES) {
      String curValue = entry.getAttr(attrName, false);
      if (OLD_VALUE.equals(curValue)) {
        attrs.put(attrName, NEW_VALUE);
      } else {
        printer.println(
            String.format(
                "    Current value of %s on cos %s is \"%s\" - not changed",
                attrName, entryName, curValue));
      }
    }

    try {
      modifyAttrs(zlc, entry, attrs);
    } catch (ServiceException e) {
      printer.printStackTrace(e);
    }
  }

  private void doAllCos(ZLdapContext zlc) throws ServiceException {
    List<Cos> coses = prov.getAllCos();
    for (Cos cos : coses) {
      doEntry(zlc, cos);
    }
  }
}
