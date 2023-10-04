// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

class DavServletTest {

  private static final int PORT = 8090;
  private static GreenMail greenMail;

  private Server server;
  private Provisioning provisioning;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.setUp();
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    provisioning = Provisioning.getInstance();
    server = JettyServerFactory.createDefault();
    server.start();
    final Account organizer =
        provisioning.createAccount(
            "organizer@" + MailboxTestUtil.DEFAULT_DOMAIN,
            "password",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    organizer.addAlias("alias@" + MailboxTestUtil.DEFAULT_DOMAIN);
    provisioning.createAccount(
        "attendee1@" + MailboxTestUtil.DEFAULT_DOMAIN,
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    provisioning.createAccount(
        "attendee2@" + MailboxTestUtil.DEFAULT_DOMAIN,
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    provisioning.createAccount(
        "attendee3@" + MailboxTestUtil.DEFAULT_DOMAIN,
        "password",
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
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
