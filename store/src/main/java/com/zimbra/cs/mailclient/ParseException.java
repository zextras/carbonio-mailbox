// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient;

/** Indicates that a parsing error occurred while reading a mail protocol response. */
public class ParseException extends MailException {
  /** Creates a new <tt>ParseException</tt> with a <tt>null</tt> detail message. */
  public ParseException() {}

  /**
   * Creates a new <tt>ParseException</tt> with the specified detail message.
   *
   * @param msg the detail message, or <tt>null</tt> if none
   */
  public ParseException(String msg) {
    super(msg);
  }
}
