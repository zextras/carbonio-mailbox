// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.account.cos;

/** Raised when the COS-count projection cannot be read or written. */
public class CosCountPersistenceException extends RuntimeException {

  public CosCountPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
