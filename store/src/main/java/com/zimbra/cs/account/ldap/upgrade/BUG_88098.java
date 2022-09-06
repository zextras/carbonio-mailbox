// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BUG_88098 extends UpgradeOp {

  @Override
  void doUpgrade() throws ServiceException {
    upgradeZimbraGalLdapFilterDef();
  }

  @SuppressWarnings("unchecked")
  private void upgradeZimbraGalLdapFilterDef() throws ServiceException {
    Config config = prov.getConfig();

    Pair<String, String> value =
        new Pair<String, String>(
            "zimbraAccountAutoComplete:(&(|(displayName=%s*)(cn=%s*)(sn=%s*)(gn=%s*)(zimbraPhoneticFirstName=%s*)(zimbraPhoneticLastName=%s*)(mail=%s*)(zimbraMailDeliveryAddress=%s*)(zimbraMailAlias=%s*))(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList)(objectclass=zimbraGroup))(!(objectclass=zimbraCalendarResource)))",
            "zimbraAccountAutoComplete:(&(|(displayName=*%s*)(cn=%s*)(sn=%s*)(gn=%s*)(zimbraPhoneticFirstName=%s*)(zimbraPhoneticLastName=%s*)(mail=%s*)(zimbraMailDeliveryAddress=%s*)(zimbraMailAlias=%s*))(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList)(objectclass=zimbraGroup))(!(objectclass=zimbraCalendarResource)))");

    Set<String> curValues = config.getMultiAttrSet(Provisioning.A_zimbraGalLdapFilterDef);

    Map<String, Object> attrs = new LinkedHashMap<String, Object>();
    String oldValue = value.getFirst();
    String newValue = value.getSecond();

    if (curValues.contains(oldValue)) {
      StringUtil.addToMultiMap(attrs, "-" + Provisioning.A_zimbraGalLdapFilterDef, oldValue);
      StringUtil.addToMultiMap(attrs, "+" + Provisioning.A_zimbraGalLdapFilterDef, newValue);
    }
    modifyAttrs(config, attrs);
  }
}
