// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.google.common.base.Charsets;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.util.MockTcpServer;

/**
 * Unit test for {@link ImapProxy}.
 *
 * @author ysasaki
 */
@Disabled 
public final class ImapProxyTest {
    private static final int PORT = 9143;
    private MockTcpServer server;

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.destroy();
        }
    }

 @Test
 @Timeout(40000)
 void bye() throws Exception {
  server = MockTcpServer.scenario()
    .sendLine("* OK server ready")
    .recvLine() // CAPABILITY
    .sendLine("* CAPABILITY IMAP4rev1 AUTH=X-ZIMBRA")
    .reply(Pattern.compile("(.*) CAPABILITY"), "{0} OK CAPABILITY\r\n")
    .recvLine() // ID
    .sendLine("* ID (\"NAME\" \"Zimbra\")")
    .reply(Pattern.compile("(.*) ID"), "{0} OK ID completed\r\n")
    .recvLine() // AUTHENTICATE
    .sendLine("+ ready for literal")
    .reply(Pattern.compile("(.*) AUTHENTICATE"), "{0} OK AUTHENTICATE\r\n")
    .recvLine() // credential
    .recvLine() // NOOP
    .reply(Pattern.compile("(.*) NOOP"), "{0} OK NOOP\r\n")
    .sendLine("* BYE server closing connection")
    .build().start(PORT);

  MockImapHandler handler = new MockImapHandler();
  ImapProxy proxy = new ImapProxy(new InetSocketAddress(PORT), "test@zimbra.com", "secret", handler);
  proxy.proxy("001", "NOOP");
  try {
   proxy.proxy("002", "NOOP");
   fail();
  } catch (ImapProxyException expected) {
  }

  // verify BYE was not proxied
  assertEquals("001 OK NOOP\r\n", handler.output.toString());

  server.shutdown(3000);
  assertEquals("C01 CAPABILITY\r\n", server.replay());
  String id = server.replay();
  assertTrue(id.matches(
    "C02 ID \\(\"name\" \"ZCS\" \"version\" \".*\" \"X-VIA\" \"127\\.0\\.0\\.1\"\\)\r\n"), id);
  assertEquals("C03 AUTHENTICATE X-ZIMBRA\r\n", server.replay());
  server.replay(); // auth token
  assertEquals("001 NOOP\r\n", server.replay());
  assertNull(server.replay());
 }
}
