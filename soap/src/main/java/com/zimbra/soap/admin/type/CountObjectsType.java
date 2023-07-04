// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;

public enum CountObjectsType {
  userAccount(true),
  account(true),
  alias(true),
  dl(true),
  domain(true),
  cos(false),
  server(false),
  calresource(true),

  // for license counting
  internalUserAccount(true),
  internalArchivingAccount(true),
  internalUserAccountX(true);

  private boolean allowsDomain;

  private CountObjectsType(boolean allowsDomain) {
    this.allowsDomain = allowsDomain;
  }

  public static CountObjectsType fromString(String type) throws ServiceException {
    try {
      // for backward compatibility, installer uses userAccounts
      if ("userAccounts".equals(type)) {
        return userAccount;
      } else {
        return CountObjectsType.valueOf(type);
      }
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("unknown count objects type: " + type, e);
    }
  }

  public static String names(String separator) {
    Joiner joiner = Joiner.on(separator);
    return joiner.join(CountObjectsType.values());
  }

  public boolean allowsDomain() {
    return allowsDomain;
  }
}
