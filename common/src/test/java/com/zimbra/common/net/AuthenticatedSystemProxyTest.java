package com.zimbra.common.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.localconfig.LC;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticatedSystemProxyTest {

  private static final String EXPECTED_AUTH =
      "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes(StandardCharsets.UTF_8));

  private ServerSocket proxy;
  private final CountDownLatch servedConnectionClosed = new CountDownLatch(1);
  private ProxySelectors.CustomProxySelector selector;
  private ProxySelector previousDelegate;
  private String previousUseSystemProxy;

  @BeforeEach
  void setUp() throws IOException {
    proxy = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
    Thread acceptor = new Thread(this::acceptConnections);
    acceptor.setDaemon(true);
    acceptor.start();

    previousUseSystemProxy = LC.client_use_system_proxy.value();
    LC.client_use_system_proxy.setDefault(true);
    selector = (ProxySelectors.CustomProxySelector) ProxySelectors.defaultProxySelector();
    previousDelegate = selector.ps;
    selector.setDefaultProxySelector(new ProxySelector() {
      @Override
      public List<Proxy> select(URI uri) {
        AuthProxy authProxy = new AuthProxy(Proxy.Type.HTTP,
            new InetSocketAddress("127.0.0.1", proxy.getLocalPort()));
        authProxy.setUsername("user");
        authProxy.setPassword("secret");
        return new ArrayList<>(List.of(authProxy));
      }

      @Override
      public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
      }
    });
  }

  @AfterEach
  void tearDown() throws IOException {
    selector.setDefaultProxySelector(previousDelegate);
    LC.client_use_system_proxy.setDefault(previousUseSystemProxy);
    proxy.close();
  }

  @Test
  void bug_authenticatedProxyConnectionStaysOpenAfterCallerClosesItsClient() throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      ClassicHttpResponse response = HttpClientUtil.executeMethod(
          client, new HttpGet("http://target.invalid/service/home"), null, null);
      assertEquals(200, response.getCode());
      EntityUtils.consume(response.getEntity());
    }

    assertFalse(servedConnectionClosed.await(1, TimeUnit.SECONDS),
        "The proxy connection closed with the caller's client: HttpClientUtil no longer builds"
            + " a separate, never-closed client for authenticated proxies. Update this test.");
  }

  private void acceptConnections() {
    while (!proxy.isClosed()) {
      try {
        Socket connection = proxy.accept();
        Thread handler = new Thread(() -> serve(connection));
        handler.setDaemon(true);
        handler.start();
      } catch (IOException e) {
        return;
      }
    }
  }

  private void serve(Socket connection) {
    boolean served = false;
    try (connection) {
      BufferedReader in = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), StandardCharsets.ISO_8859_1));
      OutputStream out = connection.getOutputStream();
      String requestLine;
      while ((requestLine = in.readLine()) != null && !requestLine.isEmpty()) {
        String auth = null;
        String header;
        while ((header = in.readLine()) != null && !header.isEmpty()) {
          if (header.regionMatches(true, 0, "Proxy-Authorization:", 0, 20)) {
            auth = header.substring(20).trim();
          }
        }
        if (EXPECTED_AUTH.equals(auth)) {
          out.write("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nok".getBytes(StandardCharsets.ISO_8859_1));
          served = true;
        } else {
          out.write(("HTTP/1.1 407 Proxy Authentication Required\r\n"
              + "Proxy-Authenticate: Basic realm=\"proxy\"\r\nContent-Length: 0\r\n\r\n")
              .getBytes(StandardCharsets.ISO_8859_1));
        }
        out.flush();
      }
    } catch (IOException ignored) {
    } finally {
      if (served) {
        servedConnectionClosed.countDown();
      }
    }
  }
}
