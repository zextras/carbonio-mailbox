// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.servlet.GuiceFilter;
import com.zextras.mailbox.util.JettyServerFactory;
import java.util.Map;
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

@Tag("api")
class HealthServletTest {

  private static Server server;
  private static final int PORT = 8080;

  @BeforeAll
  static void beforeAll() throws Exception {
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
  }

  @Test
  void liveShouldReturn200WhenHealthy() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
  }

  @Test
  void liveShouldReturn500WhenDBConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/live");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
    }
  }

    @Test
    void readyShouldReturnTrueWhenReady() throws Exception {
      try (CloseableHttpClient client = HttpClientBuilder.create()
          .build()) {
        final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");
        final CloseableHttpResponse response = client.execute(httpGet);
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        final String responseBody = new String(response.getEntity().getContent().readAllBytes());
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map responseMap = objectMapper.readValue(responseBody, Map.class);
        Assertions.assertEquals(true, responseMap.get("ready"));
      }
  }

  @Test
  void readyShouldReturnFalseWhenDatabaseConnectionFailing() throws Exception {
    try (CloseableHttpClient client = HttpClientBuilder.create()
        .build()) {
      final HttpGet httpGet = new HttpGet(server.getURI() + "/health/ready");
      final CloseableHttpResponse response = client.execute(httpGet);
      Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
      final String responseBody = new String(response.getEntity().getContent().readAllBytes());
      final ObjectMapper objectMapper = new ObjectMapper();
      final Map responseMap = objectMapper.readValue(responseBody, Map.class);
      Assertions.assertEquals(false, responseMap.get("ready"));
    }
  }


}