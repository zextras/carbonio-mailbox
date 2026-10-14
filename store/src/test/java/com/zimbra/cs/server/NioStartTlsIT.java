// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxEnvironmentSetupHelper;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.imap.ImapConfig;
import com.zimbra.cs.imap.NioImapServer;
import com.zimbra.cs.pop3.NioPop3Server;
import com.zimbra.cs.pop3.Pop3Config;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.commons.net.util.TrustManagerUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioStartTlsIT extends MailboxTestSuite {

  private static final int IMAP_PORT = 7145;
  private static final int POP3_PORT = 7111;
  private static final int TIMEOUT_MILLIS = 5000;

  private static Account account;
  private static NioImapServer imapServer;
  private static NioPop3Server pop3Server;

  @BeforeAll
  static void setUp() throws Exception {
    MailboxEnvironmentSetupHelper.setupTestKeyStore();
    Config config = Provisioning.getInstance().getConfig();
    config.setImapBindPort(IMAP_PORT);
    config.setImapCleartextLoginEnabled(false);
    config.setPop3BindPort(POP3_PORT);
    config.setPop3CleartextLoginEnabled(false);

    account = createAccount().create();

    imapServer = new NioImapServer(new ImapConfig(false), new SimpleMeterRegistry());
    imapServer.start();
    pop3Server = new NioPop3Server(new Pop3Config(false), new SimpleMeterRegistry());
    pop3Server.start();
  }

  @AfterAll
  static void tearDown() {
    imapServer.stop();
    pop3Server.stop();
  }

  @Test
  void imapShouldLoginOverTlsAfterStartTls() throws Exception {
    try (Socket socket = connect(IMAP_PORT)) {
      readLine(socket);
      write(socket, "a STARTTLS\r\n");
      String startTlsReply = readLine(socket);
      assertTrue(startTlsReply.startsWith("a OK"), "STARTTLS failed: " + startTlsReply);

      try (SSLSocket tls = startTls(socket)) {
        write(tls, "b LOGIN " + account.getName() + " " + account.getUserPassword() + "\r\n");
        List<String> response = readResponse(tls, "b");
        assertTrue(response.get(response.size() - 1).startsWith("b OK"), "LOGIN failed: " + response);
      }
    }
  }

  @Test
  void imapShouldDropCommandsPipelinedBeforeTlsHandshake() throws Exception {
    try (Socket socket = connect(IMAP_PORT)) {
      readLine(socket);
      write(socket, "a STARTTLS\r\nb NOOP\r\n");
      String startTlsReply = readLine(socket);
      assertTrue(startTlsReply.startsWith("a OK"), "STARTTLS failed: " + startTlsReply);

      try (SSLSocket tls = startTls(socket)) {
        write(tls, "c NOOP\r\n");
        List<String> response = readResponse(tls, "c");
        assertTrue(
            response.stream().noneMatch(line -> line.startsWith("b ")),
            "Command injected before TLS was executed: " + response);
      }
    }
  }

  @Test
  void pop3ShouldLoginOverTlsAfterStls() throws Exception {
    try (Socket socket = connect(POP3_PORT)) {
      readLine(socket);
      write(socket, "STLS\r\n");
      String stlsReply = readLine(socket);
      assertTrue(stlsReply.startsWith("+OK"), "STLS failed: " + stlsReply);

      try (SSLSocket tls = startTls(socket)) {
        write(tls, "USER " + account.getName() + "\r\n");
        String userReply = readLine(tls);
        assertTrue(userReply.startsWith("+OK"), "USER failed: " + userReply);
        write(tls, "PASS " + account.getUserPassword() + "\r\n");
        String reply = readLine(tls);
        assertTrue(reply.startsWith("+OK"), "PASS failed: " + reply);
      }
    }
  }

  private static Socket connect(int port) throws IOException {
    Socket socket = new Socket("localhost", port);
    socket.setSoTimeout(TIMEOUT_MILLIS);
    return socket;
  }

  private static SSLSocket startTls(Socket socket) throws Exception {
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, new TrustManager[] {TrustManagerUtils.getAcceptAllTrustManager()}, null);
    SSLSocket tls =
        (SSLSocket)
            context.getSocketFactory().createSocket(socket, "localhost", socket.getPort(), true);
    tls.startHandshake();
    return tls;
  }

  private static void write(Socket socket, String data) throws IOException {
    socket.getOutputStream().write(data.getBytes(StandardCharsets.US_ASCII));
    socket.getOutputStream().flush();
  }

  // Unbuffered on purpose: any plaintext the server sends after the STARTTLS reply must reach
  // the TLS handshake and break it, instead of being silently swallowed by a read-ahead buffer.
  private static String readLine(Socket socket) throws IOException {
    InputStream in = socket.getInputStream();
    StringBuilder line = new StringBuilder();
    int b;
    while ((b = in.read()) != '\n') {
      if (b == -1) {
        throw new EOFException("Connection closed after: " + line);
      }
      line.append((char) b);
    }
    return line.toString().stripTrailing();
  }

  private static List<String> readResponse(Socket socket, String tag) throws IOException {
    List<String> lines = new ArrayList<>();
    String line;
    do {
      line = readLine(socket);
      lines.add(line);
    } while (!line.startsWith(tag + " "));
    return lines;
  }
}
