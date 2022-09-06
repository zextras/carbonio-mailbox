// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.account.Provisioning;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BUG_72007 extends UpgradeOp {

  private static final String[] ATTRS =
      new String[] {Provisioning.A_zimbraIsSystemResource, Provisioning.A_zimbraIsSystemAccount};

  @Override
  void doUpgrade() throws ServiceException {

    Config config = prov.getConfig();
    upgrdeAcount(config.getAttr(Provisioning.A_zimbraSpamIsNotSpamAccount));
    upgrdeAcount(config.getAttr(Provisioning.A_zimbraSpamIsSpamAccount));
  }

  @Override
  Description getDescription() {
    return new Description(
        this,
        ATTRS,
        new EntryType[] {EntryType.ACCOUNT},
        null,
        ProvisioningConstants.TRUE,
        String.format(
            "Set %s of %s and %s accounts to %s",
            Arrays.deepToString(ATTRS),
            Provisioning.A_zimbraSpamIsNotSpamAccount,
            Provisioning.A_zimbraSpamIsSpamAccount,
            ProvisioningConstants.TRUE));
  }

  private void upgrdeAcount(String name) throws ServiceException {
    if (name != null) {
      Account acct = prov.get(AccountBy.name, name);
      if (acct != null) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        if (!acct.isIsSystemResource()) {
          attrs.put(Provisioning.A_zimbraIsSystemResource, ProvisioningConstants.TRUE);
        }
        if (!acct.isIsSystemAccount()) {
          attrs.put(Provisioning.A_zimbraIsSystemAccount, ProvisioningConstants.TRUE);
        }
        modifyAttrs(acct, attrs);
      }
    }
  }
}
