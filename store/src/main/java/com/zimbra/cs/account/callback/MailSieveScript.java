// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.RuleManager;
import java.util.Map;

public class MailSieveScript extends AttributeCallback {

  /** check to make sure zimbraMailHost points to a valid server zimbraServiceHostname */
  @SuppressWarnings("unchecked")
  @Override
  public void preModify(
      CallbackContext context, String attrName, Object value, Map attrsToModify, Entry entry)
      throws ServiceException {

    singleValueMod(attrName, value);

    if (!(entry instanceof Account)) return;

    Account acct = (Account) entry;

    if (!Provisioning.onLocalServer(acct)) return;

    // clear it from the in memory parsed filter rule cache
    RuleManager.clearCachedRules(acct);
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {}
}
