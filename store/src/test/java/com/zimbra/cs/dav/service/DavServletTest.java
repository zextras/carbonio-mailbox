// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.github.dockerjava.api.model.HealthCheck;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.store.StoreManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
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
  private static GreenMail greenMail;

  @Container
  GenericContainer ldapContainer =
      new GenericContainer<>(DockerImageName.parse("carbonio/ce-ldap-u20:latest"))
          .withCreateContainerCmdModifier(
              cmd -> {
                cmd.withHostName("ldap.mail.local");
                cmd.withHealthcheck(
                    new HealthCheck().withTest(List.of("nc -z localhost 2812 || exit 1")));
              })
          .withStartupTimeout(Duration.ofMinutes(3L))
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
          .withStartupTimeout(Duration.ofMinutes(3L))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("/db.sql", 0744), "/root/db.sql")
          .withExposedPorts(3306);

  private Server server;
  private Provisioning provisioning;

  @BeforeEach
  public void setUp() throws Exception {
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    LC.mysql_port.setDefault(sqlContainer.getMappedPort(3306));
    LC.ldap_port.setDefault(ldapContainer.getMappedPort(389));
    System.setProperty("java.library.path", "../native/target");
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
    MailboxManager mailboxManager = MailboxManager.getInstance();
    server = JettyServerFactory.createDefault();
    server.start();
    final String serverName = "localhost";
    provisioning.createServer(
        serverName,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
    provisioning.createDomain("test.com", new HashMap<>());
    final Account organizer =
        provisioning.createAccount(
            "organizer@test.com",
            "password",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, serverName)));
    organizer.addAlias("alias@test.com");
    provisioning.createAccount(
        "attendee1@test.com",
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, serverName)));
    provisioning.createAccount(
        "attendee2@test.com",
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, serverName)));
    provisioning.createAccount(
        "attendee3@test.com",
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, serverName)));
  }

  @AfterEach
  public void tearDown() throws Exception {
    server.stop();
    greenMail.stop();
    DbPool.shutdown();
  }

  private HttpResponse executeDavRequest(Account organizer)
      throws AuthProviderException, AuthTokenException, IOException {
    final AuthToken authToken = AuthProvider.getAuthToken(organizer);
    String url =
        "http://localhost:"
            + PORT
            + "/dav/"
            + URLEncoder.encode(organizer.getName(), StandardCharsets.UTF_8)
            + "/Calendar/95a5527e-df0a-4df2-b64a-7eee8e647efe.ics";
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    HttpPut request = new HttpPut(url);
    request.setEntity(
        new InputStreamEntity(
            Objects.requireNonNull(
                this.getClass().getResourceAsStream("Invite_ScheduleAgent_Client.ics"))));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  @Test
  void shouldNotSendNotificationWhenScheduleAgentClient()
      throws IOException, ServiceException, AuthTokenException {
    final Account organizer = provisioning.getAccount("alias@test.com");
    final HttpResponse response = executeDavRequest(organizer);

    Assertions.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    Assertions.assertEquals(0, greenMail.getReceivedMessages().length);
  }

  private static class JettyServerFactory {

    public static Server createDefault() throws Exception {
      Server server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(PORT);
      ServletContextHandler servletHandler = new ServletContextHandler();
      servletHandler.addServlet(DavServlet.class, "/*");
      server.setHandler(servletHandler);
      server.setConnectors(new Connector[] {connector});
      return server;
    }
  }
}
