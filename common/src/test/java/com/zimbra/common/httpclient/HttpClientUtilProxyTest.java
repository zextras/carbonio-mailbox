package com.zimbra.common.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import com.zimbra.common.net.ProxyHostConfiguration;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientUtilProxyTest {

  private static final String EXPECTED_AUTH =
      "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes(StandardCharsets.UTF_8));

  private HttpServer proxy;

  @BeforeEach
  void startProxy() throws Exception {
    proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    proxy.createContext("/", exchange -> {
      String auth = exchange.getRequestHeaders().getFirst("Proxy-Authorization");
      if (!EXPECTED_AUTH.equals(auth)) {
        exchange.getResponseHeaders().add("Proxy-Authenticate", "Basic realm=\"proxy\"");
        exchange.sendResponseHeaders(407, -1);
        exchange.close();
        return;
      }
      byte[] body = exchange.getRequestURI().toString().getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      exchange.getResponseBody().write(body);
      exchange.close();
    });
    proxy.start();
  }

  @AfterEach
  void stopProxy() {
    proxy.stop(0);
  }

  @Test
  void sendsRequestThroughAuthenticatedProxyWithCallersClient() throws Exception {
    AtomicInteger callerClientRequests = new AtomicInteger();
    ProxyHostConfiguration proxyConfig = new ProxyHostConfiguration();
    proxyConfig.setProxyHost("127.0.0.1");
    proxyConfig.setProxyPort(proxy.getAddress().getPort());
    proxyConfig.setUsername("user");
    proxyConfig.setPassword("secret");

    try (CloseableHttpClient client = HttpClients.custom()
        .addRequestInterceptorFirst((request, entity, context) -> callerClientRequests.incrementAndGet())
        .build()) {
      ClassicHttpResponse response = HttpClientUtil.executeWithProxy(
          client, new HttpGet("http://target.invalid/service/home"), null, proxyConfig);

      assertEquals(200, response.getCode());
      assertEquals("http://target.invalid/service/home", EntityUtils.toString(response.getEntity()));
      assertEquals(2, callerClientRequests.get());
    }
  }
}
