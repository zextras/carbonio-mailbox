// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.health.ServiceDependency.ServiceType;
import com.zextras.mailbox.servlet.HealthResponse.Dependency;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.db.DbPool;
import java.util.List;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

@Tag("api")
@Testcontainers
class HealthServletTest {

  private static final int PORT = 8080;
  private static final String DB_USER = "test";
  private static final String DB_PASSWORD = "test";
  @Container
  static MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(
      DockerImageName.parse("mariadb:10.4.31-focal"))
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
    server = new JettyServerFactory()
        .withPort(PORT)
        .addFilter("/*", new FilterHolder(GuiceFilter.class))
        .addListener(new GuiceMailboxServletConfig())
        .create();
    server.start();
  }

  @AfterEach
  void afterEach() throws Exception {
    server.stop();
  }

  @Test
  void liveShouldReturn200WhenDBConnectionOk() throws Exception {
    DbPool.startup();
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
    DbPool.shutdown();
  }

  @Test
  void liveShouldReturn500WhenDBConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
          response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void readyShouldReturn200WhenDBConnectionOk() throws Exception {
    DbPool.startup();
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
    DbPool.shutdown();
  }

  @Test
  void readyShouldReturn500WhenDBConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");

      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR,
          response.getStatusLine().getStatusCode());
    }
  }

  @Test
  @DisplayName("/health should return 200 when DB Connection OK")
  void healthRootPathShouldReturn200WhenDBConnectionOk() throws Exception {
    DbPool.startup();
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health");
      final CloseableHttpResponse response = client.execute(httpGet);

      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

      final String healthResponseBody = new String(
          response.getEntity().getContent().readAllBytes());
      final HealthResponse expected = new HealthResponse(true,
          List.of(
              new Dependency("MariaDb", ServiceType.REQUIRED, true, true)
          ));

      Assertions.assertEquals(new ObjectMapper().writeValueAsString(expected), healthResponseBody);
    }
    DbPool.shutdown();
  }
}