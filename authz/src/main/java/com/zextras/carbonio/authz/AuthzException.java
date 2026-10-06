/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

public class AuthzException extends Exception {

  public AuthzException(String message) {
    super(message);
  }

  public AuthzException(String message, Throwable cause) {
    super(message, cause);
  }
}
