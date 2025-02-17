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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LikeXmlJettyServerTest {
  private Config config;
  private com.zimbra.cs.account.Server localServer;

  @BeforeEach
  void setUp() {
    config = mock(Config.class);
    localServer = mock(com.zimbra.cs.account.Server.class);
    when(localServer.getHttpNumThreads()).thenReturn(20);
    when(localServer.getMailboxdSSLProtocols()).thenReturn(new String[] {"TLS1.2"});
    when(localServer.getSSLExcludeCipherSuites()).thenReturn(new String[] {"^TLS_RSA_.*"});
    when(localServer.getSSLIncludeCipherSuites()).thenReturn(new String[] {});
    when(localServer.getHttpConnectorMaxIdleTimeMillis()).thenReturn(3000);
  }

  @Test
  void shouldCreateJettyServer_WithFourConnectors() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    final Connector[] connectors = server.getConnectors();

    assertEquals(5, connectors.length);
  }

  @Test
  void shouldCreateJettyServer_WithOneHandler() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    assertEquals(1, server.getHandlers().length);
  }

  @Test
  void shouldCreateJettyServer_withUserConnector() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    final Connector[] connectors = server.getConnectors();

    final Connector userConnector = connectors[0];
    assertEquals("http/1.1", userConnector.getProtocols().get(0));
    assertEquals(3000, userConnector.getIdleTimeout());
  }

  @Test
  void shouldCreateJettyServer_withAdminConnector() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    final Connector[] connectors = server.getConnectors();

    final Connector adminConnector = connectors[1];
    assertEquals(2, adminConnector.getProtocols().size());
    assertEquals("ssl", adminConnector.getProtocols().get(0));
    assertEquals("http/1.1", adminConnector.getProtocols().get(1));
    assertEquals(0, adminConnector.getIdleTimeout());
  }

  @Test
  void shouldCreateJettyServer_withMTAConnector() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    final Connector[] connectors = server.getConnectors();

    final Connector adminMtaConnector = connectors[2];
    assertEquals(2, adminMtaConnector.getProtocols().size());
    assertEquals("ssl", adminMtaConnector.getProtocols().get(0));
    assertEquals("http/1.1", adminMtaConnector.getProtocols().get(1));
    assertEquals(0, adminMtaConnector.getIdleTimeout());
  }

  @Test
  void shouldCreateJettyServer_withExtensionsConnector() throws InstantiationException {
    final Server server = new Builder(config, localServer).build();

    final Connector[] connectors = server.getConnectors();

    final Connector extensionConnector = connectors[3];
    assertEquals(2, extensionConnector.getProtocols().size());
    assertEquals("ssl", extensionConnector.getProtocols().get(0));
    assertEquals("http/1.1", extensionConnector.getProtocols().get(1));
    assertEquals(3000, extensionConnector.getIdleTimeout());
  }

}