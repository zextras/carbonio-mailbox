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

public class BUG_57039 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    upgradeZimbraGalLdapAttrMap();
  }

  void upgradeZimbraGalLdapAttrMap() throws ServiceException {
    Config config = prov.getConfig();

    String attrName = Provisioning.A_zimbraGalLdapAttrMap;
    String oldValue = "zimbraCalResLocationDisplayName,displayName=zimbraCalResLocationDisplayName";
    String newValue = "zimbraCalResLocationDisplayName=zimbraCalResLocationDisplayName";

    String[] curValues = config.getMultiAttr(attrName);

    for (String value : curValues) {
      if (value.equalsIgnoreCase(oldValue)) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        StringUtil.addToMultiMap(attrs, "-" + attrName, oldValue);
        StringUtil.addToMultiMap(attrs, "+" + attrName, newValue);

        modifyAttrs(config, attrs);
      }
    }
  }
}
