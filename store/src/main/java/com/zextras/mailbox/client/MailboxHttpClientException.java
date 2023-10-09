// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

/**
 * Exception class used when {@link MailboxHttpClient} receives a failure response from a Mailbox
 * API. For example these can be cases like 404, 500, 403 and others, depending on the invoked API
 * It encapsulates the received statusCode and the reason as string. Information about the reason
 * may or may not be detailed depending on received API response.
 */
public class MailboxHttpClientException extends RuntimeException {

  private static final long serialVersionUID = -3662246841251086050L;

  private final int statusCode;
  private final String reason;

  public MailboxHttpClientException(int statusCode, String reason) {
    this.statusCode = statusCode;
    this.reason = reason;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReason() {
    return reason;
  }
}
