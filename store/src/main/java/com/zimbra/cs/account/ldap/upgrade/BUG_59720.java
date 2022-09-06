// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BUG_59720 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doAllCos(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  private void doEntry(ZLdapContext zlc, Cos cos) throws ServiceException {

    String attrName = Provisioning.A_zimbraFilterSleepInterval;
    String oldValue = "100ms";
    String newValue = "1ms";

    String curVal = cos.getAttr(attrName);
    printer.print(
        "Checking cos ["
            + cos.getName()
            + "]: "
            + "current value of "
            + attrName
            + " is "
            + curVal);

    if (newValue.equals(curVal)) {
      printer.println(" => not updating ");
      return;
    }

    Map<String, Object> attrValues = new HashMap<String, Object>();
    attrValues.put(attrName, newValue);
    try {
      printer.println(" => updating to " + newValue);
      modifyAttrs(zlc, cos, attrValues);
    } catch (ServiceException e) {
      // log the exception and continue
      printer.println("Caught ServiceException while modifying " + cos.getName());
      printer.printStackTrace(e);
    }
  }

  private void doAllCos(ZLdapContext zlc) throws ServiceException {
    List<Cos> coses = prov.getAllCos();

    for (Cos cos : coses) {
      String name = "cos " + cos.getName();
      doEntry(zlc, cos);
    }
  }
}
