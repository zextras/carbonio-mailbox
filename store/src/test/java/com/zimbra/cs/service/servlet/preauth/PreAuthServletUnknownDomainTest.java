// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.servlet.preauth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.JettyServerFactory.ServerWithConfiguration;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PreAuthServletUnknownDomainTest extends MailboxTestSuite {

  private static Server server;
  private static String preAuthUrl;

  @BeforeAll
  static void startServer() throws Exception {
    final ServerWithConfiguration configuration =
        new JettyServerFactory()
            .addServlet("/service/preauth", new ServletHolder(PreAuthServlet.class))
            .create();
    server = configuration.server();
    preAuthUrl = "http://localhost:" + configuration.serverPort() + "/service/preauth";
    server.start();
  }

  @AfterAll
  static void stopServer() throws Exception {
    server.stop();
  }

  @Test
  void shouldRespondBadRequestWhenDomainDoesNotExist() throws IOException {
    assertEquals(HttpStatus.SC_BAD_REQUEST, preAuthStatusFor("user@unknown-domain.example"));
  }

  @Test
  void shouldRespondBadRequestWhenAccountHasNoDomain() throws IOException {
    assertEquals(HttpStatus.SC_BAD_REQUEST, preAuthStatusFor("user"));
  }

  @Test
  void shouldRespondBadRequestWhenAccountDoesNotExistInKnownDomain() throws IOException {
    assertEquals(HttpStatus.SC_BAD_REQUEST, preAuthStatusFor("nobody@" + DEFAULT_DOMAIN_NAME));
  }

  private int preAuthStatusFor(String account) throws IOException {
    final String url =
        preAuthUrl
            + "?account="
            + URLEncoder.encode(account, StandardCharsets.UTF_8)
            + "&timestamp=1&expires=1&preauth=A";
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      return client.execute(new HttpGet(url)).getStatusLine().getStatusCode();
    }
  }
}
