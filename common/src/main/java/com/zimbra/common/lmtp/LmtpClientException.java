// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.lmtp;

public class LmtpClientException extends Exception {
  private static final long serialVersionUID = 1L;

  public LmtpClientException(String msg) {
    super(msg);
  }

  public LmtpClientException(Throwable e) {
    super(e);
  }

  public LmtpClientException(String msg, Throwable e) {
    super(msg, e);
  }
}
