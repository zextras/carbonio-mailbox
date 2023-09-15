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
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
class DavServletTest {

  private static final int PORT = 8090;

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

  @Container
  GenericContainer sqlContainer =
      new GenericContainer<>(DockerImageName.parse("cytopia/mariadb-10.1"))
          .withStartupTimeout(Duration.of(2, TimeUnit.MINUTES.toChronoUnit()))
          .withEnv(
              Map.of(
                  "MYSQL_USER", "zextras",
                  "MYSQL_PASSWORD", "zextras",
                  "MYSQL_ROOT_PASSWORD", "password"))
          .withCreateContainerCmdModifier(
              cmd -> {
                cmd.withHostConfig(
                    new HostConfig()
                        .withPortBindings(
                            new PortBinding(Binding.bindPort(7306), new ExposedPort(3306))));
              })
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("/db.sql", 0744), "/root/db.sql")
          .withExposedPorts(3306);

  private Server server;
  private Provisioning provisioning;
  private MailboxManager mailboxManager;

  @BeforeEach
  public void setUp() throws Exception {
    System.setProperty("log4j.configuration", "log4j-test.properties");
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(this.getClass().getResource("/localconfig-api-test.xml")).getFile());
    provisioning = Provisioning.getInstance();
    final ExecResult execResult =
        sqlContainer.execInContainer("sh", "-c", "mysql -u root -ppassword < /root/db.sql");
    if (execResult.getExitCode() != 0) {
      throw new RuntimeException(execResult.getStderr());
    }
    DbPool.startup();
    mailboxManager = MailboxManager.getInstance();
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
    DbPool.shutdown();
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
    final Account organizer = provisioning.getAccount("organizer@test.com");
    final Account attendee = provisioning.getAccount("attendee@test.com");
    final HttpResponse response = executeDavRequest(organizer);
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final Mailbox mailboxByAccount = mailboxManager.getMailboxByAccount(organizer);
  }

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
}
