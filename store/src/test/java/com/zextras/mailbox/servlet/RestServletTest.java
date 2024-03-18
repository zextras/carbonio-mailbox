// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.util.JettyServerFactory;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RestServletTest {

  private static Server server;

  @BeforeAll
  static void startUp() throws Exception {
    // See: https://docs.jboss.org/resteasy/docs/3.0.6.Final/userguide/html/Guice1.html
    server =
        new JettyServerFactory()
            .withPort(8080)
            .addListener(new GuiceResteasyBootstrapServletContextListener())
            .addServlet("/*", new ServletHolder(HttpServletDispatcher.class))
            .addInitParam("resteasy.guice.modules", "com.zextras.mailbox.servlet.RestServletModule")
            .create();
    server.start();
  }

  @AfterAll
  static void tearDown() throws Exception {
    server.stop();
  }

  @Test
  void test() {

    try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
      final HttpGet httpGet = new HttpGet("http://localhost:8080/hello/name");
      final CloseableHttpResponse execute = client.execute(httpGet);
      final int statusCode = execute.getStatusLine().getStatusCode();
      Assertions.assertEquals(200, statusCode);
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

}