// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import java.util.Map;

public class RecoveryEmailCallback extends AttributeCallback {

  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
      throws ServiceException {
    // do nothing
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {
    if (entry instanceof Account) {
      Account account = (Account) entry;
      try {
        account.unsetResetPasswordRecoveryCode();
      } catch (ServiceException e) {
        ZimbraLog.account.debug("Unable to clear ResetPasswordRecoveryCode", e);
      }
    }
  }
}
