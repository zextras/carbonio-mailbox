// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

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

public final class TcpPop3Server extends TcpServer implements Pop3Server, RealtimeStatsCallback {

  private final MeterRegistry meterRegistry;

  public TcpPop3Server(Pop3Config config, MeterRegistry meterRegistry) throws ServiceException {
    super(config);
    this.meterRegistry = meterRegistry;
    if (config.isSslEnabled()) {
      Gauge.builder(ZimbraPerf.RTS_POP_SSL_THREADS, this::numThreads).register(this.meterRegistry);
      Gauge.builder(ZimbraPerf.RTS_POP_SSL_CONN, this::numActiveHandlers)
          .register(this.meterRegistry);
    } else {
      Gauge.builder(ZimbraPerf.RTS_POP_THREADS, this::numThreads).register(this.meterRegistry);
      Gauge.builder(ZimbraPerf.RTS_POP_CONN, this::numThreads).register(this.meterRegistry);
    }
    ZimbraPerf.addStatsCallback(this);
    ServerThrottle.configureThrottle(
        config.getProtocol(),
        LC.pop3_throttle_ip_limit.intValue(),
        LC.pop3_throttle_acct_limit.intValue(),
        getThrottleSafeHosts(),
        getThrottleWhitelist());
  }

  @Override
  public String getName() {
    return getConfig().isSslEnabled() ? "Pop3SSLServer" : "Pop3Server";
  }

  @Override
  protected ProtocolHandler newProtocolHandler() {
    return new TcpPop3Handler(this, this.meterRegistry);
  }

  @Override
  public Pop3Config getConfig() {
    return (Pop3Config) super.getConfig();
  }

  /**
   * Implementation of {@link RealtimeStatsCallback} that returns the number of active handlers and
   * number of threads for this server.
   */
  @Override
  public Map<String, Object> getStatData() {
    Map<String, Object> data = new HashMap<String, Object>();
    if (getConfig().isSslEnabled()) {
      data.put(ZimbraPerf.RTS_POP_SSL_CONN, numActiveHandlers());
      data.put(ZimbraPerf.RTS_POP_SSL_THREADS, numThreads());
    } else {
      data.put(ZimbraPerf.RTS_POP_CONN, numActiveHandlers());
      data.put(ZimbraPerf.RTS_POP_THREADS, numThreads());
    }
    return data;
  }
}
