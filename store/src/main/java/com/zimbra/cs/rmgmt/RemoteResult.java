// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.rmgmt;

public class RemoteResult {
  byte[] mStdout;
  byte[] mStderr;
  int mExitStatus;
  String mExitSignal;

  public String getMExitSignal() {
    return mExitSignal;
  }

  public int getMExitStatus() {
    return mExitStatus;
  }

  public byte[] getMStderr() {
    return mStderr;
  }

  public byte[] getMStdout() {
    return mStdout;
  }
}
