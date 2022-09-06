// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BUG_68891 extends UpgradeOp {
  private static final String ATTR_NAME = Provisioning.A_zimbraGalLdapAttrMap;
  private static final String[] VALUES = {
    "zimbraDistributionListSubscriptionPolicy=zimbraDistributionListSubscriptionPolicy",
    "zimbraDistributionListUnsubscriptionPolicy=zimbraDistributionListUnsubscriptionPolicy"
  };

  @Override
  void doUpgrade() throws ServiceException {

    ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
    try {
      doGlobalConfig(zlc);
    } finally {
      LdapClient.closeContext(zlc);
    }
  }

  @Override
  Description getDescription() {
    return new Description(
        this,
        new String[] {ATTR_NAME},
        new EntryType[] {EntryType.GLOBALCONFIG},
        null,
        Arrays.deepToString(VALUES),
        String.format("Add values %s on %s", Arrays.deepToString(VALUES), ATTR_NAME));
  }

  private void doEntry(ZLdapContext zlc, Entry entry) throws ServiceException {
    String entryName = entry.getLabel();

    printer.println();
    printer.println("------------------------------");
    printer.println("Checking " + ATTR_NAME + " on " + entryName);

    Set<String> curValues = entry.getMultiAttrSet(ATTR_NAME);

    Map<String, Object> attrs = new HashMap<String, Object>();
    for (String value : VALUES) {
      if (!curValues.contains(value)) {
        StringUtil.addToMultiMap(attrs, "+" + ATTR_NAME, value);
      }
    }
    modifyAttrs(entry, attrs);
  }

  private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
    doEntry(zlc, prov.getConfig());
  }
}
