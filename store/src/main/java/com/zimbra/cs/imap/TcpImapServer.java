// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static com.zextras.mailbox.metric.Metrics.METER_REGISTRY;
import static com.zimbra.cs.imap.Metrics.IMAP_CONN;
import static com.zimbra.cs.imap.Metrics.IMAP_SSL_CONN;
import static com.zimbra.cs.imap.Metrics.IMAP_SSL_THREADS;
import static com.zimbra.cs.imap.Metrics.IMAP_THREADS;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.cs.server.ProtocolHandler;
import com.zimbra.cs.server.ServerThrottle;
import com.zimbra.cs.server.TcpServer;
import com.zimbra.cs.stats.ZimbraPerf;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;

public final class TcpImapServer extends TcpServer implements ImapServer, RealtimeStatsCallback {

  public TcpImapServer(ImapConfig config, MeterRegistry meterRegistry) throws ServiceException {
    super(config);
    if (config.isSslEnabled()) {
      Gauge.builder(IMAP_SSL_THREADS, this::numThreads).register(meterRegistry);
      Gauge.builder(IMAP_SSL_CONN, this::numActiveHandlers).register(meterRegistry);
    } else {
      Gauge.builder(IMAP_THREADS, this::numThreads).register(meterRegistry);
      Gauge.builder(IMAP_CONN, this::numThreads).register(meterRegistry);
    }
    ZimbraPerf.addStatsCallback(this);
    ServerThrottle.configureThrottle(
        config.getProtocol(),
        LC.imap_throttle_ip_limit.intValue(),
        LC.imap_throttle_acct_limit.intValue(),
        getThrottleSafeHosts(),
        getThrottleWhitelist());
  }

  @Override
  public String getName() {
    return getConfig().isSslEnabled() ? "ImapSSLServer" : "ImapServer";
  }

  @Override
  protected ProtocolHandler newProtocolHandler() {
    return new TcpImapHandler(this, METER_REGISTRY);
  }

  @Override
  public ImapConfig getConfig() {
    return (ImapConfig) super.getConfig();
  }

  /**
   * Implementation of {@link RealtimeStatsCallback} that returns the number of active handlers and
   * number of threads for this server.
   */
  @Override
  public Map<String, Object> getStatData() {
    Map<String, Object> data = new HashMap<>();
    if (getConfig().isSslEnabled()) {
      data.put(ZimbraPerf.RTS_IMAP_SSL_CONN, numActiveHandlers());
      data.put(ZimbraPerf.RTS_IMAP_SSL_THREADS, numThreads());
    } else {
      data.put(ZimbraPerf.RTS_IMAP_CONN, numActiveHandlers());
      data.put(ZimbraPerf.RTS_IMAP_THREADS, numThreads());
    }
    return data;
  }
}
