// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.gal;

import com.zimbra.common.service.ServiceException;

public enum GalOp {
  autocomplete,
  search,
  sync;

  public static GalOp fromString(String s) throws ServiceException {
    try {
      return GalOp.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("invalid GAL op: " + s, e);
    }
  }
}
