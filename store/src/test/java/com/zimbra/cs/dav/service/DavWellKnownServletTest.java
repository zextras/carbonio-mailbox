// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.JettyServerFactory.ServerWithConfiguration;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DavWellKnownServletTest extends MailboxTestSuite {

  private static Server server;
  private static String baseUrl;

  @BeforeAll
  static void startServer() throws Exception {
    final ServerWithConfiguration configuration =
        new JettyServerFactory()
            .addServlet("/.well-known/*", new ServletHolder(DavWellKnownServlet.class))
            .create();
    server = configuration.server();
    baseUrl = "http://localhost:" + configuration.serverPort();
    server.start();
  }

  @AfterAll
  static void stopServer() throws Exception {
    server.stop();
  }

  @Test
  void shouldRespondNotFoundWhenThereIsNoPathAfterWellKnown() throws IOException {
    assertEquals(HttpStatus.SC_NOT_FOUND, get("/.well-known").getStatusLine().getStatusCode());
  }

  @Test
  void shouldRespondNotFoundForUnknownWellKnownPath() throws IOException {
    assertEquals(HttpStatus.SC_NOT_FOUND, get("/.well-known/unknown").getStatusLine().getStatusCode());
  }

  @Test
  void shouldRedirectCalDavToDavService() throws IOException {
    assertRedirectsToDav(get("/.well-known/caldav"));
  }

  @Test
  void shouldRedirectCardDavToDavServiceIgnoringCase() throws IOException {
    assertRedirectsToDav(get("/.well-known/CardDAV"));
  }

  private static void assertRedirectsToDav(HttpResponse response) {
    assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, response.getStatusLine().getStatusCode());
    final Header location = response.getFirstHeader(HttpHeaders.LOCATION);
    assertTrue(location.getValue().endsWith(DavServlet.DAV_PATH));
  }

  private static HttpResponse get(String path) throws IOException {
    try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build()) {
      return client.execute(new HttpGet(baseUrl + path));
    }
  }
}
