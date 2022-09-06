// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.account.accesscontrol.generated.UserRights;

/**
 * bridging class so we don't have to include the "generated" classes at callsites, because that's
 * kind of ugly.
 */
public abstract class Rights {

  public static class Admin extends AdminRights {}

  public static class User extends UserRights {}
}
