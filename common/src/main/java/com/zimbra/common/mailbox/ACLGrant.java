// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

public interface ACLGrant {
  public String getGranteeId();

  public String getGranteeName();

  public GrantGranteeType getGrantGranteeType();

  public String getPermissions();
}
