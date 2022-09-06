// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import com.zimbra.common.stats.DeltaCalculator;

public class JmxImapDaemonStats implements JmxImapDaemonStatsMBean {

  private final DeltaCalculator imapDeltaCalc = new DeltaCalculator(ZimbraPerf.STOPWATCH_IMAP);

  JmxImapDaemonStats() {}

  @Override
  public long getImapRequests() {
    return ZimbraPerf.STOPWATCH_IMAP.getCount();
  }

  @Override
  public long getImapResponseMs() {
    return (long) imapDeltaCalc.getRealtimeAverage();
  }

  @Override
  public void reset() {
    imapDeltaCalc.reset();
  }
}
