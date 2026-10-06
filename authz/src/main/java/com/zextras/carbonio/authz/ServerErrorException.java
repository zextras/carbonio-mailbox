/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

public class ServerErrorException extends AuthzException {

  public ServerErrorException(String message) {
    super(message);
  }
}
