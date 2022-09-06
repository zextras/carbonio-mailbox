// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/** */
package com.zimbra.common.util;

import com.zimbra.common.util.Log.Level;

public class AccountLogger {
  private String mAccountName;
  private String mCategory;
  private Level mLevel;

  public AccountLogger(String category, String accountName, Level level) {
    mCategory = category;
    mAccountName = accountName;
    mLevel = level;
  }

  public String getAccountName() {
    return mAccountName;
  }

  public String getCategory() {
    return mCategory;
  }

  public Level getLevel() {
    return mLevel;
  }
}
