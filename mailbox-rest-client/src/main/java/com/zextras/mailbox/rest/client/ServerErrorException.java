/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rest.client;

public class ServerErrorException extends MailboxRestException {

  public ServerErrorException(String message) {
    super(message);
  }
}
