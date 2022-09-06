// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import java.util.Map;

public class AdminGroup extends AttributeCallback {

  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
      throws ServiceException {}

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {

    if (!(entry instanceof DistributionList)) return;

    Provisioning prov = Provisioning.getInstance();
    if (!(prov instanceof LdapProv)) return;

    DistributionList group = (DistributionList) entry;
    ((LdapProv) prov).removeFromCache(group);
  }
}
