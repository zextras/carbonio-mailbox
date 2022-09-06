// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class ImapSearchTooComplexException extends ImapParseException {
  private static final long serialVersionUID = 1105373916072686233L;

  public ImapSearchTooComplexException(String tag, String message) {
    super(tag, message);
  }
}
