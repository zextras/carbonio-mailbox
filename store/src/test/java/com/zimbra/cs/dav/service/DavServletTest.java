// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.service.AuthProvider;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DavServletTest {

  private static Provisioning provisioning;
  private static Account organizer;
  private static Account attendee;
  public String testName;
  public Server server;
  private static final int PORT = 8090;
  private static final String CALENDAR_UID = "95a5527e-df0a-4df2-b64a-7eee8e647efe";
  private static final String FREE_BUSY_UID = "bb7bc421-bdde-4da6-987b-b17c5d343307";
  private static final String DAV_BASE_PATH = "/dav";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    ScheduledTaskManager.startup();
    provisioning = Provisioning.getInstance();
  }

  private static class JettyServerFactory {

    public static Server createDefault() throws Exception {
      Server server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(PORT);
      ServletContextHandler servletHandler = new ServletContextHandler();
      servletHandler.addServlet(DavServlet.class, DAV_BASE_PATH + "/*");
      server.setHandler(servletHandler);
      server.setConnectors(new Connector[] {connector});
      return server;
    }
  }

  @AfterEach
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    server = JettyServerFactory.createDefault();
    server.start();
    provisioning = Provisioning.getInstance();
    organizer = provisioning.createAccount("test@test.com", "password", new HashMap<>());
    attendee = provisioning.createAccount("attendee@test.com", "password", new HashMap<>());
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

  private HttpResponse requestAttendeeFreeBusy() throws Exception {
    HttpClient client = createHttpClient();
    HttpPost request = new HttpPost(getSentResourceUrl());
    request.setEntity(
        new InputStreamEntity(
            Objects.requireNonNull(
                this.getClass().getResourceAsStream("FreeBusyRequest_" + FREE_BUSY_UID + ".ics"))));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    request.setHeader(DavProtocol.HEADER_RECIPIENT, "mailto:" + attendee.getName());
    request.setHeader(DavProtocol.HEADER_ORIGINATOR, "mailto:" + organizer.getName());
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

  private String getSentResourceUrl() {
    return "http://localhost:"
        + PORT
        + DAV_BASE_PATH
        + "/home/"
        + URLEncoder.encode(organizer.getName(), StandardCharsets.UTF_8)
        + "/Sent";
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

  /**
   * Added for bug CO-823: request freebusy of attendee
   *
   * @throws Exception
   */
  @Test
  void shouldReturnUIDWhenRequestingFreeBusyOfAttendee() throws Exception {
    Calendar calendar = new CalendarBuilder().build(this.getClass().getResourceAsStream("FreeBusyRequest_" + FREE_BUSY_UID + ".ics"), StandardCharsets.UTF_8.toString());

    Assertions.assertEquals(
        HttpStatus.SC_CREATED, createAppointmentWithCalDAV().getStatusLine().getStatusCode());
    final HttpResponse response = requestAttendeeFreeBusy();
    Assertions.assertEquals(
        HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final String freeBusyResponse = new String(response.getEntity().getContent().readAllBytes());
    System.out.println(freeBusyResponse);
    Assertions.assertTrue(freeBusyResponse.contains("UID:" + FREE_BUSY_UID));
  }
}
