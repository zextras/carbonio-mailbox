// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DavServletTest {

  private static final int PORT = 8090;
  private static final String CALENDAR_UID = "95a5527e-df0a-4df2-b64a-7eee8e647efe";
  private static final String DAV_BASE_PATH = "/dav";
  private static GreenMail greenMail;
  private static Account organizer;

  private static Server server;
  private static Provisioning provisioning;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    provisioning = Provisioning.getInstance();
    server =
        JettyServerFactory.create(
            PORT, Map.of(DAV_BASE_PATH + "/*", new ServletHolder(DavServlet.class)));
    server.start();
    organizer =
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

  @AfterAll
  public static void tearDown() throws Exception {
    server.stop();
    greenMail.stop();
    MailboxTestUtil.tearDown();
  }

  private HttpResponse createInviteWithDavRequest(Account organizer)
      throws AuthProviderException, AuthTokenException, IOException {
    final AuthToken authToken = AuthProvider.getAuthToken(organizer);
    String url =
        "http://localhost:"
            + PORT
            + DAV_BASE_PATH
            + "/home/"
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
    final HttpResponse response = createInviteWithDavRequest(organizer);

    Assertions.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    Assertions.assertEquals(0, greenMail.getReceivedMessages().length);
  }

  private HttpResponse createAppointmentWithCalDAV() throws Exception {
    HttpClient client = createHttpClient();
    HttpPut request = new HttpPut(getCalDavResourceUrl());
    request.setEntity(
        new InputStreamEntity(
            Objects.requireNonNull(
                this.getClass().getResourceAsStream(DavServletTest.CALENDAR_UID + ".ics"))));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  private HttpResponse deleteAppointmentWithCalDAV() throws Exception {
    HttpClient client = createHttpClient();
    HttpDelete request = new HttpDelete(getCalDavResourceUrl());
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  private HttpResponse getAppointmentWithCalDAV() throws Exception {
    HttpClient client = createHttpClient();
    HttpGet request = new HttpGet(getCalDavResourceUrl());
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  /**
   * Returns CalDav Resource URL for this test suite {@link #organizer} and calendar {@link
   * #CALENDAR_UID}
   *
   * @return url endpoint to make the request
   */
  private String getCalDavResourceUrl() {
    return "http://localhost:"
        + PORT
        + DAV_BASE_PATH
        + "/home/"
        + URLEncoder.encode(organizer.getName(), StandardCharsets.UTF_8)
        + "/Calendar/"
        + DavServletTest.CALENDAR_UID
        + ".ics";
  }

  private HttpClient createHttpClient() throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(organizer);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }

  /**
   * Added for bug CO-839 (create appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldCreateAppointmentUsingCalDAV() throws Exception {
    final HttpResponse createResponse = createAppointmentWithCalDAV();
    Assertions.assertEquals(HttpStatus.SC_CREATED, createResponse.getStatusLine().getStatusCode());
    final HttpResponse appointmentWithCalDAV = getAppointmentWithCalDAV();
    Assertions.assertEquals(
        HttpStatus.SC_OK, appointmentWithCalDAV.getStatusLine().getStatusCode());
    final String createdAppointment =
        new String(getAppointmentWithCalDAV().getEntity().getContent().readAllBytes());
    Assertions.assertTrue(createdAppointment.contains(CALENDAR_UID));
  }

  /**
   * Added for bug CO-840 (delete appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldDeleteAppointmentUsingCalDAV() throws Exception {
    Assertions.assertEquals(
        HttpStatus.SC_CREATED, createAppointmentWithCalDAV().getStatusLine().getStatusCode());
    Assertions.assertEquals(
        HttpStatus.SC_OK, getAppointmentWithCalDAV().getStatusLine().getStatusCode());
    Assertions.assertEquals(
        HttpStatus.SC_NO_CONTENT, deleteAppointmentWithCalDAV().getStatusLine().getStatusCode());
    Assertions.assertEquals(
        HttpStatus.SC_NOT_FOUND, getAppointmentWithCalDAV().getStatusLine().getStatusCode());
  }
}
