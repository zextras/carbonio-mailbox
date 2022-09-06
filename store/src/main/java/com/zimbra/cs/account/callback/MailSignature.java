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
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Signature;
import com.zimbra.cs.account.callback.CallbackContext.DataKey;
import java.util.Map;

public class MailSignature extends AttributeCallback {

  /** check to make sure zimbraPrefMailSignature is shorter than the limit */
  @Override
  public void preModify(
      CallbackContext context, String attrName, Object value, Map attrsToModify, Entry entry)
      throws ServiceException {

    SingleValueMod mod = singleValueMod(attrName, value);
    if (mod.unsetting()) return;

    if (entry != null
        && !((entry instanceof Account)
            || (entry instanceof Identity)
            || (entry instanceof Signature))) {
      return;
    }

    long maxLen = -1;

    String maxInContext = context.getData(DataKey.MAX_SIGNATURE_LEN);
    if (maxInContext != null) {
      try {
        maxLen = Integer.parseInt(maxInContext);
      } catch (NumberFormatException e) {
        ZimbraLog.account.warn(
            "encountered invalid " + DataKey.MAX_SIGNATURE_LEN.name() + ": " + maxInContext);
      }
    }

    if (maxLen == -1) {
      String maxInAttrsToModify =
          (String) attrsToModify.get(Provisioning.A_zimbraMailSignatureMaxLength);
      if (maxInAttrsToModify != null) {
        try {
          maxLen = Integer.parseInt(maxInAttrsToModify);
        } catch (NumberFormatException e) {
          ZimbraLog.account.warn(
              "encountered invalid "
                  + Provisioning.A_zimbraMailSignatureMaxLength
                  + ": "
                  + maxInAttrsToModify);
        }
      }
    }

    if (maxLen == -1) {
      if (entry == null) {
        return;
      }

      Account account;
      if (entry instanceof Account) {
        account = (Account) entry;
      } else if (entry instanceof Identity) {
        account = ((Identity) entry).getAccount();
      } else if (entry instanceof Signature) {
        account = ((Signature) entry).getAccount();
      } else {
        return;
      }

      maxLen = account.getMailSignatureMaxLength();
    }

    // 0 means unlimited
    if (maxLen != 0 && ((String) value).length() > maxLen) {
      throw ServiceException.INVALID_REQUEST(
          Provisioning.A_zimbraPrefMailSignature + " is longer than the limited value " + maxLen,
          null);
    }
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {}
}
