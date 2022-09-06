// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

public class GranteeFlag {

  // allowed for admin rights
  public static final short F_ADMIN = 0x0001;

  public static final short F_INDIVIDUAL = 0x0002;
  public static final short F_GROUP = 0x0004;
  public static final short F_DOMAIN = 0x0008;
  public static final short F_AUTHUSER = 0x0010;
  public static final short F_PUBLIC = 0x0020;

  public static final short F_IS_ZIMBRA_ENTRY = 0x0040;

  public static final short F_HAS_SECRET = 0x0080;
}
