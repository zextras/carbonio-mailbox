// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.server.NioConnection;
import com.zimbra.cs.server.NioHandler;
import com.zimbra.cs.server.NioOutputStream;
import com.zimbra.cs.stats.ZimbraPerf;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException;

final class NioImapHandler extends ImapHandler implements NioHandler {
  private final ImapConfig config;
  private final NioConnection connection;
  private NioImapRequest request;

  NioImapHandler(NioImapServer server, NioConnection conn) {
    super(server.getConfig());
    connection = conn;
    config = server.getConfig();
    output = conn.getOutputStream();
  }

  @Override
  protected String getRemoteIp() {
    return connection.getRemoteAddress().getAddress().getHostAddress();
  }

  @Override
  public void connectionOpened() throws IOException {
    sendGreeting();
  }

  @Override
  public void messageReceived(Object msg) throws IOException, ProtocolDecoderException {
    if (request == null) {
      request = new NioImapRequest(this);
    }

    if (request.parse(msg)) {
      // Request is complete
      try {
        if (!processRequest(request)) {
          dropConnection();
        }
      } finally {
        if (request != null) {
          request.cleanup();
          request = null;
        }
      }
      if (LC.imap_max_consecutive_error.intValue() > 0
          && consecutiveError >= LC.imap_max_consecutive_error.intValue()) {
        ZimbraLog.imap.error(
            "NIO:zimbraImapMaxConsecutiveError exceeded %d",
            LC.imap_max_consecutive_error.intValue());
        dropConnection();
      }
    }
  }

  @Override
  public void exceptionCaught(Throwable e) throws IOException {
    try {
      if (e instanceof javax.net.ssl.SSLException) {
        ZimbraLog.imap.error("NIO:Error detected by SSL subsystem, dropping connection:%s", e);
        dropConnection(false); // Bug 79904 prevent using SSL port in plain text
      } else if (e instanceof NioImapDecoder.TooBigLiteralException) {
        NioImapDecoder.TooBigLiteralException tble = (NioImapDecoder.TooBigLiteralException) e;
        /* 'tble' has access to the buffer of the IMAP command read so far, which may
         * be useful if better, context sensitive error reporting is desired.  See its getMessage()
         * method for an example of special handling for APPEND
         */
        String tag = (request != null) ? tag = request.getTag() : tble.getRequestTag();
        sendBAD(tag, e.getMessage());
      } else if (e instanceof RecoverableProtocolDecoderException) {
        sendBAD("*", e.getMessage());
      } else if (e instanceof ProtocolDecoderException) {
        sendBAD("*", e.getMessage());
        dropConnection(true);
      }
    } finally {
      if (request != null) {
        request.cleanup();
        request = null;
      }
    }
  }

  private boolean processRequest(NioImapRequest req) throws IOException {
    ImapListener i4selected = selectedFolderListener;
    if (i4selected != null) {
      i4selected.updateAccessTime();
    }

    long start = ZimbraPerf.STOPWATCH_IMAP.start();

    try {
      if (!checkAccountStatus()) {
        return false;
      }
      if (authenticator != null && !authenticator.isComplete()) {
        return continueAuthentication(req);
      }
      try {
        return executeRequest(req);
      } catch (ImapProxyException e) {
        ZimbraLog.imap.debug("proxy failed", e);
        sendNO(req.getTag(), "Shared folder temporally unavailable");
        return false; // disconnect
      } catch (ImapParseException e) {
        handleParseException(e);
        return true;
      } catch (ImapException e) { // session closed
        ZimbraLog.imap.debug("stop processing", e);
        return false;
      } catch (Exception e) { // something's wrong
        ZimbraLog.imap.error("unexpected exception", e);
        sendBAD("Unknown Error");
        return false;
      }
    } finally {
      long elapsed = ZimbraPerf.STOPWATCH_IMAP.stop(start);
      if (lastCommand != null) {
        ZimbraPerf.IMAP_TRACKER.addStat(lastCommand.toUpperCase(), start);
        ZimbraPerf.IMAP_TRACKER_PROMETHEUS.addStat(lastCommand.toUpperCase(), start);

        ZimbraPerf.IMAPD_TRACKER.addStat(lastCommand.toUpperCase(), start);
        ZimbraPerf.IMAPD_TRACKER_PROMETHEUS.addStat(lastCommand.toUpperCase(), start);

        ZimbraLog.imap.info("%s elapsed=%d (NIO)", lastCommand.toUpperCase(), elapsed);
      } else {
        ZimbraLog.imap.info("(unknown) elapsed=%d (NIO)", elapsed);
      }
    }
  }

  @Override
  public void dropConnection() {
    dropConnection(true);
  }

  /**
   * Called when connection is closed. No need to worry about concurrent execution since requests
   * are processed in sequence for any given connection.
   */
  @Override
  public void connectionClosed() {
    if (request != null) {
      request.cleanup();
      request = null;
    }
    try {
      unsetSelectedFolder(false);
    } catch (Exception ignore) {
    } finally {
      logout();
    }
  }

  @Override
  public void connectionIdle() {
    ZimbraLog.imap.debug("dropping NIO connection for inactivity");
    dropConnection();
  }

  @Override
  public void setLoggingContext() {
    super.setLoggingContext();
    ZimbraLog.addConnectionIdToContext(String.valueOf(connection.getId()));
  }

  @Override
  protected void sendLine(String line, boolean flush) throws IOException {
    NioOutputStream out = (NioOutputStream) output;
    if (out != null) {
      out.write(line);
      out.write(LINE_SEPARATOR_BYTES);
      if (flush) {
        out.flush();
      }
    }
  }

  /**
   * Close the connection.
   *
   * <p>Do not clean up the session here, but let the framework call {@link #connectionClosed()}, so
   * that concurrency issues are taken care of.
   */
  @Override
  protected void dropConnection(boolean sendBanner) {
    if (credentials != null && !goodbyeSent) {
      ZimbraLog.imap.info(
          "NIO:dropping connection for user %s (server-initiated)", credentials.getUsername());
    }
    if (!connection.isOpen()) {
      return; // No longer connected
    }
    if (sendBanner && !goodbyeSent) {
      sendBYE();
    }
    connection.close();
  }

  @Override
  protected void close() {
    dropConnection(true);
  }

  @Override
  protected void enableInactivityTimer() {
    connection.setMaxIdleSeconds(config.getAuthenticatedMaxIdleTime());
  }

  @Override
  protected void completeAuthentication() throws IOException {
    if (authenticator.isEncryptionEnabled()) {
      connection.startSasl(authenticator.getSaslServer());
    }
    authenticator.sendSuccess();
  }

  @Override
  protected boolean doSTARTTLS(String tag) throws IOException {
    if (!checkState(tag, State.NOT_AUTHENTICATED)) {
      return true;
    } else if (startedTLS) {
      sendNO(tag, "TLS already started");
      return true;
    }

    connection.startTls();
    sendOK(tag, "begin TLS negotiation now");
    startedTLS = true;
    return true;
  }

  @Override
  protected InetSocketAddress getLocalAddress() {
    return connection.getLocalAddress();
  }
}
