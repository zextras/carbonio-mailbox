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

public class BUG_43779 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    Config config = prov.getConfig();

    String[] value = {
      "zimbraGroupAutoComplete:(&(|(displayName=%s*)(cn=%s*)(sn=%s*)(gn=%s*)(mail=%s*)(zimbraMailDeliveryAddress=%s*)(zimbraMailAlias=%s*))(objectclass=zimbraDistributionList))",
      "zimbraGroupSync:(&(|(displayName=*%s*)(cn=*%s*)(sn=*%s*)(gn=*%s*)(mail=*%s*)(zimbraMailDeliveryAddress=*%s*)(zimbraMailAlias=*%s*))(objectclass=zimbraDistributionList))",
      "zimbraGroups:(&(|(displayName=*%s*)(cn=*%s*)(sn=*%s*)(gn=*%s*)(mail=*%s*)(zimbraMailDeliveryAddress=*%s*)(zimbraMailAlias=*%s*))(objectclass=zimbraDistributionList))"
    };

    Map<String, Object> attr = new HashMap<String, Object>();
    attr.put("+" + Provisioning.A_zimbraGalLdapFilterDef, value);

    printer.println(
        "Adding zimbraGroupAutoComplete, zimbraGroupSync, and zimbraGroups filters to global config"
            + " "
            + Provisioning.A_zimbraGalLdapFilterDef);
    prov.modifyAttrs(config, attr);
  }
}
