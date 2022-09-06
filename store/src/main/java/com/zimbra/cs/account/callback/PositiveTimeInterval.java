// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import java.util.Map;

public class PositiveTimeInterval extends AttributeCallback {

  @SuppressWarnings("rawtypes")
  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
      throws ServiceException {
    SingleValueMod mod = singleValueMod(attrName, attrValue);
    if (mod.setting()) {
      long interval = DateUtil.getTimeInterval(mod.value(), 0);
      if (interval <= 0) {
        throw ServiceException.INVALID_REQUEST(
            "cannot set " + attrName + " less than or equal to 0", null);
      }
    }
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {}
}
