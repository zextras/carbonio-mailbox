// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.LikeXmlJettyServer.Builder;
import com.zextras.mailbox.LikeXmlJettyServer.InstantiationException;
import com.zimbra.cs.account.Config;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;

class LikeXmlJettyServerTest {

  @Test
  void shouldCreateJettyServer() throws InstantiationException {
    final Config config = mock(Config.class);
    when(config.getHttpNumThreads()).thenReturn(20);
    when(config.getMailboxdSSLProtocols()).thenReturn(new String[] {"TLS1.2"});
    when(config.getSSLExcludeCipherSuites()).thenReturn(new String[] {"^TLS_RSA_.*"});
    when(config.getSSLIncludeCipherSuites()).thenReturn(new String[] {});
    when(config.getHttpConnectorMaxIdleTimeMillis()).thenReturn(3000);

    final Server server = new Builder(config).build();

    assertEquals(1, server.getHandlers().length);
    final Connector[] connectors = server.getConnectors();
    assertEquals(4, connectors.length);
    final Connector userConnector = connectors[0];

    assertEquals("http/1.1", userConnector.getProtocols().get(0));
    assertEquals(3000, userConnector.getIdleTimeout());

    final Connector adminConnector = connectors[1];
    assertEquals(2, adminConnector.getProtocols().size());
    assertEquals("ssl", adminConnector.getProtocols().get(0));
    assertEquals("http/1.1", adminConnector.getProtocols().get(1));
    assertEquals(0, adminConnector.getIdleTimeout());

    final Connector adminMtaConnector = connectors[2];
    assertEquals(2, adminMtaConnector.getProtocols().size());
    assertEquals("ssl", adminMtaConnector.getProtocols().get(0));
    assertEquals("http/1.1", adminMtaConnector.getProtocols().get(1));
    assertEquals(0, adminMtaConnector.getIdleTimeout());

    final Connector extensionConnector = connectors[3];
    assertEquals(2, extensionConnector.getProtocols().size());
    assertEquals("ssl", extensionConnector.getProtocols().get(0));
    assertEquals("http/1.1", extensionConnector.getProtocols().get(1));
    assertEquals(3000, extensionConnector.getIdleTimeout());
  }

}