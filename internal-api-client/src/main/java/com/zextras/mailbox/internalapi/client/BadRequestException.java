/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.internalapi.client;

public class BadRequestException extends InternalApiException {

  public BadRequestException(String message) {
    super(message);
  }
}
