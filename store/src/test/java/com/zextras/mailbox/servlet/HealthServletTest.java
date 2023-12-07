// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("api")
@Testcontainers
class HealthServletTest {

  private static final int PORT = 8080;
  private static Server server;
  private static final String DB_USER = "test";
  private static final String DB_PASSWORD = "test";

  @Container
  static MariaDBContainer mariaDBContainer = new MariaDBContainer()
      .withUsername(DB_USER)
      .withPassword(DB_PASSWORD)
      .withDatabaseName("zimbra");

  @BeforeAll
  static void beforeAll() throws Exception {
    LC.zimbra_mysql_password.setDefault(mariaDBContainer.getUsername());
    LC.zimbra_mysql_user.setDefault(mariaDBContainer.getPassword());
    LC.mysql_bind_address.setDefault("127.0.0.1");
    LC.mysql_port.setDefault(mariaDBContainer.getFirstMappedPort());
    DbPool.startup();
    server = new JettyServerFactory()
        .withPort(PORT)
        .addFilter("/*", new FilterHolder(GuiceFilter.class))
        .addListener(new GuiceMailboxServletConfig())
        .create();
    server.start();
  }

  @AfterAll
  static void afterAll() throws Exception {
    server.stop();
    DbPool.shutdown();
  }

  @Test
  void liveShouldReturn200WhenDbPoolStarted() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void liveShouldReturn500WhenDbPoolNotStarted() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
          response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void readyShouldReturnTrueWhenDBConnectionOk() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void readyShouldReturnFalseWhenDatabaseConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
  }


}