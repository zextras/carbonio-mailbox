// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import static com.zextras.mailbox.metric.Metrics.METER_REGISTRY;

import io.micrometer.core.instrument.Gauge;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.cs.server.NioConnection;
import com.zimbra.cs.server.NioHandler;
import com.zimbra.cs.server.NioServer;
import com.zimbra.cs.server.ServerThrottle;
import com.zimbra.cs.stats.ZimbraPerf;

public final class NioPop3Server extends NioServer implements Pop3Server, RealtimeStatsCallback {
    private static final ProtocolDecoder DECODER = new TextLineDecoder(Charsets.ISO_8859_1, LineDelimiter.AUTO);

    public NioPop3Server(Pop3Config config) throws ServiceException {
        super(config);
        if (config.isSslEnabled()) {
            Gauge.builder(ZimbraPerf.RTS_POP_SSL_THREADS, this::getNumConnections).register(METER_REGISTRY);
            Gauge.builder(ZimbraPerf.RTS_POP_SSL_CONN, this::getNumThreads).register(METER_REGISTRY);
        } else {
            Gauge.builder(ZimbraPerf.RTS_POP_THREADS, this::getNumThreads).register(METER_REGISTRY);
            Gauge.builder(ZimbraPerf.RTS_POP_CONN, this::getNumConnections).register(METER_REGISTRY);
        }
        registerMBean(getName());
        ZimbraPerf.addStatsCallback(this);
        ServerThrottle.configureThrottle(config.getProtocol(), LC.pop3_throttle_ip_limit.intValue(), LC.pop3_throttle_acct_limit.intValue(), getThrottleSafeHosts(), getThrottleWhitelist());
    }

    @Override
    public String getName() {
        return config.isSslEnabled() ? "Pop3SSLServer" : "Pop3Server";
    }

    @Override
    public NioHandler createHandler(NioConnection conn) {
        return new NioPop3Handler(this, conn);
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
                return DECODER;
            }
        };
    }

    @Override
    public Pop3Config getConfig() {
        return (Pop3Config) super.getConfig();
    }

    @Override
    public Map<String, Object> getStatData() {
        String connStatName = getConfig().isSslEnabled() ? ZimbraPerf.RTS_POP_SSL_CONN : ZimbraPerf.RTS_POP_CONN;
        String threadStatName = getConfig().isSslEnabled() ? ZimbraPerf.RTS_POP_SSL_THREADS : ZimbraPerf.RTS_POP_THREADS;
        return ImmutableMap.of(connStatName, (Object) getNumConnections(), threadStatName, getNumThreads());
    }
}
