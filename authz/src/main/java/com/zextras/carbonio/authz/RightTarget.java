/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

public record RightTarget(String type, String id) {

  public static RightTarget account(String accountId) {
    return new RightTarget("account", accountId);
  }

  public static RightTarget domain(String domainId) {
    return new RightTarget("domain", domainId);
  }

  public static RightTarget cos(String cosId) {
    return new RightTarget("cos", cosId);
  }
}
