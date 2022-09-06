// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

/**
 * Exception thrown when session notification encounters a message which cannot be properly
 * renumbered This typically occurs when mailbox.item_id_checkpoint is inconsistent due to earlier
 * manual DB modification See bug 46549 and bug 77780 for more details on the sequence of events
 * which results in this bad state
 */
public class ImapRenumberException extends RuntimeException {

  private static final long serialVersionUID = 6406289034846208672L;

  public ImapRenumberException() {
    super();
  }

  public ImapRenumberException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImapRenumberException(String message) {
    super(message);
  }
}
