// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.lmtp;

public class LmtpProtocolException extends Exception {
  private static final long serialVersionUID = 1L;

  public LmtpProtocolException(String msg) {
    super(msg);
  }
}
