// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import java.util.concurrent.ThreadFactory;

public class TcpThreadFactory implements ThreadFactory {
  private int count = 0;
  private final String prefix;
  private final boolean isDaemon;
  private final int priority;

  public TcpThreadFactory(String prefix, boolean isDaemon) {
    this(prefix, isDaemon, Thread.NORM_PRIORITY);
  }

  public TcpThreadFactory(String prefix, boolean isDaemon, int priority) {
    this.prefix = prefix;
    this.isDaemon = isDaemon;
    this.priority = priority;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    int n;
    synchronized (this) {
      n = ++count;
    }
    StringBuffer sb = new StringBuffer(prefix);
    sb.append('-').append(n);
    Thread t = new Thread(runnable, sb.toString());
    t.setDaemon(isDaemon);
    t.setPriority(priority);
    return t;
  }
}
