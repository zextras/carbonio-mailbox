// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoServiceStatistics;

/**
 * A wrapper of {@link IoServiceStatistics} to expose it as a MBean.
 *
 * @author ysasaki
 */
final class NioServerStats implements NioServerStatsMBean {
    private final IoAcceptor acceptor;
    private final IoServiceStatistics stats;

    NioServerStats(NioServer server) {
        acceptor = server.acceptor;
        stats = acceptor.getStatistics();
    }

    @Override
    public long getTotalSessions() {
        return stats.getCumulativeManagedSessionCount();
    }

    @Override
    public long getActiveSessions() {
        return acceptor.getManagedSessionCount();
    }

    @Override
    public long getReadBytes() {
        return stats.getReadBytes();
    }

    @Override
    public long getReadMessages() {
        return stats.getReadMessages();
    }

    @Override
    public long getWrittenBytes() {
        return stats.getWrittenBytes();
    }

    @Override
    public long getWrittenMessages() {
        return stats.getWrittenMessages();
    }

    @Override
    public long getScheduledWriteBytes() {
        return stats.getScheduledWriteBytes();
    }

    @Override
    public long getScheduledWriteMessages() {
        return stats.getScheduledWriteMessages();
    }
}
