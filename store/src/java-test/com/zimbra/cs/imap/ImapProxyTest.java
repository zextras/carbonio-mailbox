// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.zimbra.cs.util.MockTcpServer;

import junit.framework.Assert;

/**
 * Unit test for {@link ImapProxy}.
 *
 * @author ysasaki
 */
@Ignore 
public final class ImapProxyTest {
    private static final int PORT = 9143;
    private MockTcpServer server;

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.destroy();
        }
    }

    @Test(timeout = 40000)
    public void bye() throws Exception {
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
            Assert.fail();
        } catch (ImapProxyException expected) {
        }

        // verify BYE was not proxied
        Assert.assertEquals("001 OK NOOP\r\n", handler.output.toString());

        server.shutdown(3000);
        Assert.assertEquals("C01 CAPABILITY\r\n", server.replay());
        String id = server.replay();
        Assert.assertTrue(id, id.matches(
                "C02 ID \\(\"name\" \"ZCS\" \"version\" \".*\" \"X-VIA\" \"127\\.0\\.0\\.1\"\\)\r\n"));
        Assert.assertEquals("C03 AUTHENTICATE X-ZIMBRA\r\n", server.replay());
        server.replay(); // auth token
        Assert.assertEquals("001 NOOP\r\n", server.replay());
        Assert.assertEquals(null, server.replay());
    }
}
