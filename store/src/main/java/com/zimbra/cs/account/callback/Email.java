// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.Entry;
import java.util.Map;

/**
 * Callback for validating attributes that should've been declared email in attrs.xml but had been
 * declared as string.
 *
 * <p>To avoid LDAP upgrade complication, we use this callback for validating the format. If the
 * attr had been declared as email, the validation would have happened in AttributeInfo.checkValue.
 */
public class Email extends AttributeCallback {

  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
      throws ServiceException {

    SingleValueMod mod = singleValueMod(attrsToModify, attrName);
    if (mod.unsetting()) return;

    AttributeInfo.validEmailAddress(mod.value(), false);
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {}
}
