// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.db.DbPool;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("api")
@Testcontainers
class HealthServletTest {

   private static final String DB_USER = "test";
  private static final String DB_PASSWORD = "test";

  @Container
  static MariaDBContainer<?> mariaDBContainer =
      new MariaDBContainer<>(DockerImageName.parse("mariadb:10.4.31-focal"))
          .withUsername(DB_USER)
          .withPassword(DB_PASSWORD)
          .withDatabaseName("zimbra");

  private static Server server;

  @BeforeEach
  void beforeEach() throws Exception {
    LC.zimbra_mysql_password.setDefault(mariaDBContainer.getUsername());
    LC.zimbra_mysql_user.setDefault(mariaDBContainer.getPassword());
    LC.mysql_bind_address.setDefault(mariaDBContainer.getHost());
    LC.mysql_port.setDefault(mariaDBContainer.getFirstMappedPort());
    server =
        new JettyServerFactory()
             .addFilter("/*", new FilterHolder(GuiceFilter.class))
            .addListener(new GuiceMailboxServletConfig())
            .create().server();
    server.start();
  }

  @AfterEach
  void afterEach() throws Exception {
    if (server != null) server.stop();
  }

  @Test
  void liveShouldReturn200WhenDBConnectionOk() throws Exception {
    withDb(
        () -> {
          try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");

            final CloseableHttpResponse response = client.execute(httpGet);

            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
          }
        });
  }

  @Test
  void liveShouldReturn500WhenDBConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void readyShouldReturn200WhenDBConnectionOk() throws Exception {
    withDb(
        () -> {
          try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");

            final CloseableHttpResponse response = client.execute(httpGet);

            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
          }
        });
  }

  @Test
  void readyShouldReturn500WhenDBConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    }
  }

  @Test
  @DisplayName("/health should return 200 when DB Connection OK")
  void healthRootPathShouldReturn200WhenDBConnectionOk() throws Exception {
    withDb(
        () -> {
          try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final HttpGet httpGet = new HttpGet(server.getURI() + "/health");
            final CloseableHttpResponse response = client.execute(httpGet);

            Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            final String healthResponseBody =
                new String(response.getEntity().getContent().readAllBytes());

            Assertions.assertEquals(
                "{\"ready\":true,\"dependencies\":[{\"name\":\"MariaDb\",\"type\":\"REQUIRED\",\"ready\":true,\"live\":true}]}",
                healthResponseBody);
          }
        });
  }

  private void withDb(ThrowingRunnable runnable) throws Exception {
    DbPool.startup();
    try {
      runnable.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      DbPool.shutdown();
    }
  }

  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
