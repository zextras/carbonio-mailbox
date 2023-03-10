// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import com.google.common.collect.Maps;
import com.zimbra.common.jetty.JettyMonitor;
import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import java.util.Map;
import org.eclipse.jetty.util.thread.ThreadPool;

/** Returns stats about Jetty threads and connections. */
public class JettyStats implements RealtimeStatsCallback {

  private static final Log log = LogFactory.getLog(JettyStats.class);

  @Override
  public Map<String, Integer> getStatData() {
    ThreadPool pool = JettyMonitor.getThreadPool();
    if (pool == null) {
      log.debug("Thread pool has not been initialized.  Not returning stat data.");
      return null;
    }
    Map<String, Object> data = Maps.newHashMap();
    data.put(ZimbraPerf.RTS_HTTP_THREADS, pool.getThreads());
    data.put(ZimbraPerf.RTS_HTTP_IDLE_THREADS, pool.getIdleThreads());
    return data;
  }
}
