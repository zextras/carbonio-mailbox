// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;

public class BUG_42877 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    Config config = prov.getConfig();

    String[] values = {
      "facsimileTelephoneNumber,fax=workFax",
      "homeTelephoneNumber,homePhone=homePhone",
      "mobileTelephoneNumber,mobile=mobilePhone",
      "pagerTelephoneNumber,pager=pager"
    };

    Map<String, Object> attr = new HashMap<String, Object>();
    attr.put("+" + Provisioning.A_zimbraGalLdapAttrMap, values);

    printer.println(
        "Adding workFax, homePhone, mobilePhone, pager attr maps to global config "
            + Provisioning.A_zimbraGalLdapAttrMap);
    prov.modifyAttrs(config, attr);
  }
}
