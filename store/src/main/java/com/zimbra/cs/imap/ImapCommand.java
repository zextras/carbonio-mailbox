// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public abstract class ImapCommand {
  private long createTime;

  public ImapCommand() {
    this.createTime = System.currentTimeMillis();
  }

  public long getCreateTime() {
    return createTime;
  }

  protected boolean hasSameParams(ImapCommand command) {
    return this.equals(command);
  }

  protected boolean isDuplicate(ImapCommand command) {
    return this.getClass().equals(command.getClass()) && this.hasSameParams(command);
  }

  protected boolean throttle(ImapCommand previousCommand) {
    return false;
  }
}
