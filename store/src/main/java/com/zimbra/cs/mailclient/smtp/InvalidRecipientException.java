// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.smtp;

import com.zimbra.cs.mailclient.CommandFailedException;

@SuppressWarnings("serial")
final class InvalidRecipientException extends CommandFailedException {

  private String recipient;

  InvalidRecipientException(String recipient, String serverError) {
    super(SmtpConnection.RCPT, "Invalid recipient " + recipient + ": " + serverError);
    this.recipient = recipient;
  }

  String getRecipient() {
    return recipient;
  }
}
