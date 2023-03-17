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

import com.google.common.collect.ImmutableMap;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.server.NioConnection;
import com.zimbra.cs.server.NioHandler;
import com.zimbra.cs.server.NioServer;
import com.zimbra.cs.server.ServerThrottle;
import com.zimbra.cs.stats.ZimbraPerf;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public final class NioImapServer extends NioServer implements ImapServer, RealtimeStatsCallback {
  private final NioImapDecoder decoder;

  public NioImapServer(ImapConfig config, MeterRegistry meterRegistry) throws ServiceException {
    super(config);
    if (config.isSslEnabled()) {
      Gauge.builder(IMAP_SSL_THREADS, this::getNumThreads).register(meterRegistry);
      Gauge.builder(IMAP_SSL_CONN, this::getNumConnections).register(meterRegistry);
    } else {
      Gauge.builder(IMAP_THREADS, this::getNumThreads).register(meterRegistry);
      Gauge.builder(IMAP_CONN, this::getNumConnections).register(meterRegistry);
    }
    decoder = new NioImapDecoder(config);
    registerMBean(getName());
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
    return config.isSslEnabled() ? "ImapSSLServer" : "ImapServer";
  }

  @Override
  public NioHandler createHandler(NioConnection conn) {
    return new NioImapHandler(this, conn, METER_REGISTRY);
  }

  @Override
  protected ProtocolCodecFactory getProtocolCodecFactory() {
    return new ProtocolCodecFactory() {
      @Override
      public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return DEFAULT_ENCODER;
      }

      @Override
      public ProtocolDecoder getDecoder(IoSession session) {
        return decoder;
      }
    };
  }

  @Override
  public ImapConfig getConfig() {
    return (ImapConfig) super.getConfig();
  }

  @Override
  public Log getLog() {
    return ZimbraLog.imap;
  }

  @Override
  public Map<String, Object> getStatData() {
    String connStatName =
        getConfig().isSslEnabled() ? ZimbraPerf.RTS_IMAP_SSL_CONN : ZimbraPerf.RTS_IMAP_CONN;
    String threadStatName =
        getConfig().isSslEnabled() ? ZimbraPerf.RTS_IMAP_SSL_THREADS : ZimbraPerf.RTS_IMAP_THREADS;
    return ImmutableMap.of(
        connStatName, (Object) getNumConnections(), threadStatName, getNumThreads());
  }
}
