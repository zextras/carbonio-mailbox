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

public class BUG_55649 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    upgradeZimbraGalLdapAttrMap();
  }

  private void upgradeZimbraGalLdapAttrMap() throws ServiceException {

    String valueToAdd =
        "binary"
            + " zimbraPrefMailSMIMECertificate,userCertificate,userSMIMECertificate=SMIMECertificate";

    Config config = prov.getConfig();

    Set<String> curValues = config.getMultiAttrSet(Provisioning.A_zimbraGalLdapAttrMap);
    if (curValues.contains(valueToAdd)) {
      return;
    }

    Map<String, Object> attrs = new HashMap<String, Object>();
    StringUtil.addToMultiMap(attrs, "+" + Provisioning.A_zimbraGalLdapAttrMap, valueToAdd);

    modifyAttrs(config, attrs);
  }
}
