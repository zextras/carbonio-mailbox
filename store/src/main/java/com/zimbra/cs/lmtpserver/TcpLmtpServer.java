// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import static com.zimbra.cs.lmtpserver.Metrics.LMTP_CONN;
import static com.zimbra.cs.lmtpserver.Metrics.LMTP_THREADS;

import com.zextras.mailbox.metric.Metrics;
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

public final class TcpLmtpServer extends TcpServer implements LmtpServer, RealtimeStatsCallback {
  public TcpLmtpServer(LmtpConfig config, MeterRegistry meterRegistry) throws ServiceException {
    super(config);
    ZimbraPerf.addStatsCallback(this);
    Gauge.builder(LMTP_THREADS, this::numThreads).register(meterRegistry);
    Gauge.builder(LMTP_CONN, this::numActiveHandlers).register(meterRegistry);
    ServerThrottle.configureThrottle(
        config.getProtocol(),
        LC.lmtp_throttle_ip_limit.intValue(),
        0,
        getThrottleSafeHosts(),
        getThrottleWhitelist());
  }

  @Override
  public String getName() {
    return "LmtpServer";
  }

  @Override
  protected ProtocolHandler newProtocolHandler() {
    return new TcpLmtpHandler(this, Metrics.METER_REGISTRY);
  }

  @Override
  public LmtpConfig getConfig() {
    return (LmtpConfig) super.getConfig();
  }

  /**
   * Implementation of {@link RealtimeStatsCallback} that returns the number of active handlers and
   * number of threads for this server.
   */
  @Override
  public Map<String, Object> getStatData() {
    Map<String, Object> data = new HashMap<>();
    data.put(ZimbraPerf.RTS_LMTP_CONN, numActiveHandlers());
    data.put(ZimbraPerf.RTS_LMTP_THREADS, numThreads());
    return data;
  }
}
