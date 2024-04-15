// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.LikeXmlJettyServer.Builder;
import com.zextras.mailbox.LikeXmlJettyServer.InstantiationException;
import com.zextras.mailbox.config.GlobalConfigProvider;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;

class LikeXmlJettyServerTest {

  @Test
  void shouldCreateJettyServer() throws ServiceException, InstantiationException {
    final GlobalConfigProvider mock = mock(GlobalConfigProvider.class);
    final Config config = mock(Config.class);
    when(mock.get()).thenReturn(config);
    when(config.getHttpNumThreads()).thenReturn(20);
    when(config.getMailboxdSSLProtocols()).thenReturn(new String[] {"TLS1.2"});
    when(config.getSSLExcludeCipherSuites()).thenReturn(new String[] {"^TLS_RSA_.*"});
    when(config.getSSLIncludeCipherSuites()).thenReturn(new String[] {});

    final Server server = new Builder(mock).build();
    assertEquals(1, server.getHandlers().length);
  }

}