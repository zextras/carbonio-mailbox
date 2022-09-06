// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

public interface AppSpecificPasswordData {

  public String getName();

  public String getPassword();

  public Long getDateCreated();

  public Long getDateLastUsed();
}
