// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

public interface NioServerStatsMBean {
  long getTotalSessions();

  long getActiveSessions();

  long getReadBytes();

  long getReadMessages();

  long getWrittenBytes();

  long getWrittenMessages();

  long getScheduledWriteBytes();

  long getScheduledWriteMessages();
}
