// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.db.DbPool;
import io.netty.handler.codec.http.HttpMethod;
import io.vavr.control.Try;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.trilead.ssh2.crypto.Base64;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;

@Tag("api")
@Testcontainers
class HealthServletTest {

  private static final int PORT = 8080;
  private static final String DB_USER = "test";
  private static final String DB_PASSWORD = "test";

  private MockServerClient serviceDiscoverMock;

  @Container
  static MariaDBContainer<?> mariaDBContainer =
      new MariaDBContainer<>(DockerImageName.parse("mariadb:10.4.31-focal"))
          .withUsername(DB_USER)
          .withPassword(DB_PASSWORD)
          .withDatabaseName("zimbra");

  @Container
  static RabbitMQContainer messageBrokerContainer = new RabbitMQContainer("rabbitmq:3.13.4")
      .withExtraHost("test","127.78.0.7")
      .withExposedPorts(20005);

  private static Server server;

  @BeforeEach
  void beforeEach() throws Exception {
    LC.zimbra_mysql_password.setDefault(mariaDBContainer.getUsername());
    LC.zimbra_mysql_user.setDefault(mariaDBContainer.getPassword());
    LC.mysql_bind_address.setDefault(mariaDBContainer.getHost());
    LC.mysql_port.setDefault(mariaDBContainer.getFirstMappedPort());
    server =
        new JettyServerFactory()
            .withPort(PORT)
            .addFilter("/*", new FilterHolder(GuiceFilter.class))
            .addListener(new GuiceMailboxServletConfig())
            .create();
    server.start();

    serviceDiscoverMock = new MockServerClient("localhost", 8500);
    final String encodedAdminUsername = new String(Base64.encode(messageBrokerContainer.getAdminUsername().getBytes()));
    final String encodedAdminPassword = new String(Base64.encode(messageBrokerContainer.getAdminPassword().getBytes()));
    final String bodyPayloadFormat = "[{\"Key\":\"%s\",\"Value\":\"%s\"}]";
    serviceDiscoverMock
        .when(
            HttpRequest.request()
                .withMethod(HttpMethod.GET.toString())
                .withPath("/v1/kv/carbonio-message-broker/default/password")
                .withHeader("X-Consul-Token", "fake-token"))
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(
                    String.format(
                        bodyPayloadFormat, "carbonio-message-broker/default/password", encodedAdminPassword)));

    serviceDiscoverMock
        .when(
            HttpRequest.request()
                .withMethod(HttpMethod.GET.toString())
                .withPath("/v1/kv/carbonio-message-broker/default/username")
                .withHeader("X-Consul-Token", "fake-token"))
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody(
                    String.format(
                        bodyPayloadFormat, "carbonio-message-broker/default/username", encodedAdminUsername)));

    MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
    mockedFiles.when(() -> Files.readString(any(Path.class))).thenReturn("fake-token");
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
