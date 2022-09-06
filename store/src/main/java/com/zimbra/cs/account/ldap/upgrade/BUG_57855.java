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

public class BUG_57855 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    upgradeZimbraGalLdapFilterDef();
  }

  void upgradeZimbraGalLdapFilterDef() throws ServiceException {
    Config config = prov.getConfig();

    String attrName = Provisioning.A_zimbraGalLdapFilterDef;
    String[] addValues =
        new String[] {
          "email_has:(mail=*%s*)",
          "email2_has:(mail=*%s*)",
          "email3_has:(mail=*%s*)",
          "department_has:(ou=*%s*)"
        };

    Set<String> curValues = config.getMultiAttrSet(attrName);

    Map<String, Object> attrs = new HashMap<String, Object>();
    for (String value : addValues) {
      if (!curValues.contains(value)) {
        StringUtil.addToMultiMap(attrs, "+" + attrName, value);
      }
    }

    modifyAttrs(config, attrs);
  }
}
