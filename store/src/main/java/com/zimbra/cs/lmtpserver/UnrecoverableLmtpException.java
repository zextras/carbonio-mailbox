// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

/** Handling of this type of exception should be to simply drop the connection. */
public class UnrecoverableLmtpException extends Exception {

  public UnrecoverableLmtpException(String message) {
    super(message);
  }

  public UnrecoverableLmtpException(String message, Throwable cause) {
    super(message, cause);
  }
}
