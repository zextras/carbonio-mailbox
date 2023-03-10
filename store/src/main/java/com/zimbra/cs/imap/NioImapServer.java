// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

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
import java.util.Map;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public final class NioImapServer extends NioServer implements ImapServer, RealtimeStatsCallback {
  private final NioImapDecoder decoder;

  public NioImapServer(ImapConfig config) throws ServiceException {
    super(config);
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
    return new NioImapHandler(this, conn);
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
  public Map<String, Integer> getStatData() {
    String connStatName =
        getConfig().isSslEnabled() ? ZimbraPerf.RTS_IMAP_SSL_CONN : ZimbraPerf.RTS_IMAP_CONN;
    String threadStatName =
        getConfig().isSslEnabled() ? ZimbraPerf.RTS_IMAP_SSL_THREADS : ZimbraPerf.RTS_IMAP_THREADS;
    return ImmutableMap.of(connStatName, getNumConnections(), threadStatName, getNumThreads());
  }
}
