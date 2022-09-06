// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import com.zimbra.cs.redolog.op.RedoableOp;

public class MailboxIdConflictException extends RedoException {
  private static final long serialVersionUID = -4186818816051395390L;

  private String mAccountId;
  private int mExpectedId;
  private int mFoundId;

  public MailboxIdConflictException(String accountId, int expectedId, int foundId, RedoableOp op) {
    super(
        "Mailbox ID for account "
            + accountId
            + " changed unexpectedly to "
            + foundId
            + "; expected "
            + expectedId,
        op);
    mAccountId = accountId;
    mExpectedId = expectedId;
    mFoundId = foundId;
  }

  public String getAccountId() {
    return mAccountId;
  }

  public int getExpectedId() {
    return mExpectedId;
  }

  public int getFoundId() {
    return mFoundId;
  }
}
