// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import com.zimbra.cs.security.sasl.SaslFilter;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import javax.security.sasl.SaslServer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.filter.ssl.ZimbraSslFilter;

public final class NioConnection {
  private final NioServer server;
  private final IoSession session;
  private final OutputStream out;
  private final InetSocketAddress remoteAddress;
  private ZimbraSslFilter tlsSslFilter;

  NioConnection(NioServer server, IoSession session) {
    this.server = server;
    this.session = session;
    remoteAddress = (InetSocketAddress) session.getRemoteAddress();
    out =
        new NioOutputStream(
            session,
            server.getConfig().getWriteChunkSize(),
            server.getConfig().getNioMaxWriteQueueSize(),
            server.getConfig().getNioMaxWriteQueueDelay());
  }

  /** Returns the connection ID. */
  public long getId() {
    return session.getId();
  }

  public OutputStream getOutputStream() {
    return out;
  }

  public NioServer getServer() {
    return server;
  }

  public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) session.getServiceAddress();
  }

  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  public void setMaxIdleSeconds(int secs) {
    session.getConfig().setBothIdleTime(secs);
  }

  public boolean isTlsStartedIfNecessary() {
    return tlsSslFilter == null || tlsSslFilter.isSslHandshakeComplete(session);
  }

  public void startTls() {
    tlsSslFilter = server.newSSLFilter();
    session.getFilterChain().addFirst("ssl", tlsSslFilter);
    session.setAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE, true);
  }

  public void startSasl(SaslServer sasl) {
    SaslFilter filter = new SaslFilter(sasl);
    session.getFilterChain().addFirst("sasl", filter);
    session.setAttribute(SaslFilter.DISABLE_ENCRYPTION_ONCE, true);
  }

  public void send(Object obj) {
    session.write(obj);
  }

  public long getScheduledWriteBytes() {
    return session.getScheduledWriteBytes();
  }

  public boolean isOpen() {
    return session.isConnected();
  }

  public void close() {
    session.close(false);
  }
}
