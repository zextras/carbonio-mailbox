/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rest.client;

public class MailboxRestException extends Exception {

  public MailboxRestException(String message) {
    super(message);
  }

  public MailboxRestException(String message, Throwable cause) {
    super(message, cause);
  }
}
