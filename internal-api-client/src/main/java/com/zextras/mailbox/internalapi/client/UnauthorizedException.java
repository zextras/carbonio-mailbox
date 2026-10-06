/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.internalapi.client;

public class UnauthorizedException extends InternalApiException {

  public UnauthorizedException(String message) {
    super(message);
  }
}
