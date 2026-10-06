/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.internalapi.client;

public class InternalApiException extends Exception {

  public InternalApiException(String message) {
    super(message);
  }

  public InternalApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
