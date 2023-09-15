// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HealthCheck;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class DavServletTest {

  private static final int PORT = 8090;
  private Server server;
  private Provisioning provisioning;

  private static class JettyServerFactory {

    public static Server createDefault() throws Exception {
      Server server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(PORT);
      ServletHandler servletHandler = new ServletHandler();
      servletHandler.addServletWithMapping(DavServlet.class, "/dav");
      server.setHandler(servletHandler);
      server.setConnectors(new Connector[] {connector});
      return server;
    }
  }

  @Container
  GenericContainer ldapContainer =
      new GenericContainer<>(DockerImageName.parse("carbonio/ce-ldap-u20:latest"))
          .withCreateContainerCmdModifier(
              cmd -> {
                cmd.withHostName("ldap.mail.local");
                cmd.withHealthcheck(
                    new HealthCheck().withTest(List.of("nc -z localhost 2812 || exit 1")));
                cmd.withHostConfig(
                    new HostConfig()
                        .withPortBindings(
                            new PortBinding(Binding.bindPort(389), new ExposedPort(389))));
              })
          .withExposedPorts(389);

  @BeforeEach
  public void setUp() throws Exception {
    System.setProperty("log4j.configuration", "log4j-test.properties");
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(this.getClass().getResource("/localconfig-api-test.xml")).getFile());
    provisioning = Provisioning.getInstance();
    server = JettyServerFactory.createDefault();
    server.start();
    final String serverName = "localhost";
    provisioning.createServer(
        serverName,
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT);
          }
        });
    provisioning.createDomain("test.com", new HashMap<>());
    provisioning.createAccount(
        "organizer@test.com",
        "password",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraMailHost, serverName);
          }
        });
    provisioning.createAccount(
        "attendee@test.com",
        "password",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraMailHost, serverName);
          }
        });
  }

  @AfterEach
  public void tearDown() throws Exception {
    server.stop();
  }

  private HttpResponse executeDavRequest(Account organizer)
      throws AuthProviderException, AuthTokenException, IOException {
    final AuthToken authToken = AuthProvider.getAuthToken(organizer);
    String url = "http://localhost:" + PORT + "/dav";
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    HttpGet request = new HttpGet(url);
    return client.execute(request);
  }

  @Test
  void shouldNotSendNotificationWhenScheduleAgentClient()
      throws IOException, ServiceException, AuthTokenException {

    final HttpResponse response = executeDavRequest(provisioning.getAccount("organizer@test.com"));
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }
}
