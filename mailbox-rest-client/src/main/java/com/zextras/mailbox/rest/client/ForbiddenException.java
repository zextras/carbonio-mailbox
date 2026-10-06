/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rest.client;

public class ForbiddenException extends MailboxRestException {

  public ForbiddenException(String message) {
    super(message);
  }
}
