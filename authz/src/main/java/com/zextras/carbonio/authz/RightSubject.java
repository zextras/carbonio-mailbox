/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

public record RightSubject(String token, String accountId) {

  public static RightSubject token(String token) {
    return new RightSubject(token, null);
  }

  public static RightSubject account(String accountId) {
    return new RightSubject(null, accountId);
  }
}
