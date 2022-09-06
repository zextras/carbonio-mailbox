// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BUG_58481 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    upgradeZimbraGalLdapAttrMap();
  }

  private void upgradeZimbraGalLdapAttrMap() throws ServiceException {
    final String attrName = Provisioning.A_zimbraGalLdapAttrMap;

    final String[] valuesToAdd =
        new String[] {
          "objectClass=objectClass", "zimbraId=zimbraId", "zimbraMailForwardingAddress=member"
        };

    Config config = prov.getConfig();

    Map<String, Object> attrs = new HashMap<String, Object>();

    Set<String> curValues = config.getMultiAttrSet(attrName);

    for (String valueToAdd : valuesToAdd) {
      if (!curValues.contains(valueToAdd)) {
        StringUtil.addToMultiMap(attrs, "+" + attrName, valueToAdd);
      }
    }

    modifyAttrs(config, attrs);
  }
}
