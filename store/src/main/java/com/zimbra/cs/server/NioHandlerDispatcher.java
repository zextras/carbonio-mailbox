// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import com.google.common.base.Charsets;
import java.io.IOException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderException;

/**
 * Handler for MINA I/O events. Responsible for notifying the connection's {@link NioHandler} when a
 * connection has been opened, closed, become idle, or a new request has been received.
 */
final class NioHandlerDispatcher extends IoHandlerAdapter {
  private final NioServer server;

  NioHandlerDispatcher(NioServer server) {
    this.server = server;
  }

  /**
   * Invoked from an I/O processor thread when a new connection has been created. Because this
   * method is supposed to be called from the same thread that handles I/O of multiple sessions,
   * please implement this method to perform tasks that consumes minimal amount of time such as
   * socket parameter and user-defined session attribute initialization.
   */
  @Override
  public void sessionCreated(IoSession session) throws IOException {
    NioConnection conn = new NioConnection(server, session);
    session.setAttribute(NioConnection.class, conn);
    session.setAttribute(NioHandler.class, server.createHandler(conn));
  }

  /**
   * Invoked when a connection has been opened. This method is invoked after {@link
   * #sessionCreated(IoSession)}. The biggest difference from {@link #sessionCreated(IoSession)} is
   * that it's invoked from a handler thread instead of an I/O processor thread.
   */
  @Override
  public void sessionOpened(IoSession session) throws IOException {
    NioHandler handler = getHandler(session);
    if (!server.config.isServiceEnabled()) {
      server.getLog().warn("Dropping connection (user services are disabled)");
      session.close(true);
    } else if (server.acceptor.getManagedSessionCount() > server.getConfig().getMaxConnections()) {
      server.getLog().warn("Dropping connection (max connections exceeded)");
      String message = server.getConfig().getConnectionRejected();
      if (message != null) {
        session.write(IoBuffer.wrap((message + "\r\n").getBytes(Charsets.ISO_8859_1)));
      }
      session.close(false);
    } else {
      handler.connectionOpened();
    }
  }

  @Override
  public void sessionClosed(IoSession session) throws IOException {
    getHandler(session).connectionClosed();
    getConnection(session).close();
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) throws IOException {
    getHandler(session).connectionIdle();
  }

  @Override
  public void messageReceived(IoSession session, Object msg)
      throws IOException, ProtocolDecoderException {
    NioConnection conn = getConnection(session);
    if (conn != null && !conn.isTlsStartedIfNecessary()) {
      server.getLog().warn("plaintext message received during starttls handshake");
      return;
    }
    getHandler(session).messageReceived(msg);
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable e) throws IOException {
    getHandler(session).exceptionCaught(e);
  }

  public static NioHandler getHandler(IoSession session) {
    return (NioHandler) session.getAttribute(NioHandler.class);
  }

  public static NioConnection getConnection(IoSession session) {
    return (NioConnection) session.getAttribute(NioConnection.class);
  }
}
