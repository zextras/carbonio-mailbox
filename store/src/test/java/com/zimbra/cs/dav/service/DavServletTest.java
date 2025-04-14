// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service;

import static com.icegreen.greenmail.util.ServerSetup.PROTOCOL_SMTP;
import static com.zimbra.cs.mailclient.smtp.SmtpConfig.DEFAULT_HOST;
import static com.zimbra.cs.mailclient.smtp.SmtpConfig.DEFAULT_PORT;
import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.JettyServerFactory.ServerWithConfiguration;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.CalDavCreateAppointmentRequestBuilder;
import com.zimbra.cs.dav.CalDavFreeBusyRequestBuilder;
import com.zimbra.cs.service.AuthProvider;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class DavServletTest extends MailboxTestSuite {

  private static Server server;
  private static final String DAV_BASE_PATH = "/dav";
  private static String davBaseUrl;
  private static GreenMail greenMail;
  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  public static void setUp() throws Exception {
    greenMail = new GreenMail(new ServerSetup[]{ new ServerSetup(DEFAULT_PORT, DEFAULT_HOST, PROTOCOL_SMTP) });
    greenMail.start();
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
    final ServerWithConfiguration serverConfig = new JettyServerFactory()
        .addServlet(DAV_BASE_PATH + "/*", new ServletHolder(DavServlet.class))
        .create();
    davBaseUrl = "http://localhost:" + serverConfig.serverPort() + DAV_BASE_PATH;
    server = serverConfig.server();
    server.start();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    server.stop();
    greenMail.stop();
  }

  @BeforeEach
  void beforeEach() {
    greenMail.reset();
  }

  @Test
  void shouldNotSendNotificationWhenScheduleAgentClient() throws Exception {
    Account organizer = getRandomAccountForDefaultDomain();
    organizer.addAlias("alias@" + mailboxTestExtension.getDefaultDomain());

    final HttpPut request = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .organizer("alias@" + mailboxTestExtension.getDefaultDomain())
        .scheduleAgent(ScheduleAgent.CLIENT)
        .addAttendee(getRandomAccountForDefaultDomain())
        .addAttendee(getRandomAccountForDefaultDomain())
        .addAttendee(getRandomAccountForDefaultDomain())
        .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  private Account getRandomAccountForDefaultDomain() throws ServiceException {
    return accountCreatorFactory.get().create();
  }

  @Test
  void shouldSendNotificationsForEachAttendeeWhenScheduleAgentIsServer() throws Exception {
    Account organizer = getRandomAccountForDefaultDomain();

    final HttpPut request = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .organizer(organizer)
        .scheduleAgent(ScheduleAgent.SERVER)
        .addAttendee(getRandomAccountForDefaultDomain())
        .addAttendee(getRandomAccountForDefaultDomain())
        .addAttendee(getRandomAccountForDefaultDomain())
        .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
    assertEquals(3, greenMail.getReceivedMessages().length);
  }

  @Test
  void shouldCreateAppointment() throws Exception {
    Account organizer = getRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    HttpPut createAppointmentRequest = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .uuid(calendarUUID)
        .organizer(organizer)
        .build();

    HttpResponse createAppointmentResponse = createHttpClientWith(organizer).execute(createAppointmentRequest);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(createAppointmentResponse));
  }

  @Test
  void shouldGetACreatedAppointment() throws Exception {
    Account organizer = getRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    createAppointment(organizer, calendarUUID);

    HttpResponse getAppointmentResponse = getAppointment(organizer, calendarUUID);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(getAppointmentResponse));
    assertTrue(readContentFrom(getAppointmentResponse).contains("UID:" + calendarUUID));
  }

  @Test
  void shouldDeleteAppointment() throws Exception {
    Account organizer = getRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    createAppointment(organizer, calendarUUID);

    HttpResponse deleteAppointmentResponse = deleteAppointment(organizer, calendarUUID);

    assertEquals(HttpStatus.SC_NO_CONTENT, statusCodeFrom(deleteAppointmentResponse));
    assertEquals(HttpStatus.SC_NOT_FOUND, statusCodeFrom(getAppointment(organizer, calendarUUID)));
  }

  @Test
  void createAnAppointmentAndFindThatSlotAsBusyStatus() throws Exception {
    Account busyPerson = getRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .organizer(busyPerson)
        .addAttendee(busyPerson)
        .timeslot("20231207T124500", "20231207T144500")
        .build();
    createHttpClientWith(busyPerson).execute(createAppointmentRequest);

    UUID calendarId = UUID.randomUUID();
    Account calendarViewer = getRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest = new CalDavFreeBusyRequestBuilder(davBaseUrl)
        .asThunderbird()
        .uuid(calendarId)
        .originatorEmail(calendarViewer.getName())
        .recipientEmail(busyPerson.getName())
        .timeslot("20231206T114500", "20231208T154500")
        .build();
    HttpResponse freeBusyResponse = createHttpClientWith(calendarViewer).execute(freeBusyRequest);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(freeBusyResponse));
    String content = readContentFrom(freeBusyResponse);
    assertTrue(content.contains("UID:" + calendarId));
    assertTrue(content.contains("FREEBUSY;FBTYPE=BUSY:20231207T124500Z/20231207T144500Z"));
  }

  @Test
  void createAnAppointmentAndFindThatSlotAsBusyStatusUsingICalendar() throws Exception {
    Account busyPerson = getRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .organizer(busyPerson)
        .addAttendee(busyPerson)
        .timeslot("20231207T124500", "20231207T144500")
        .build();
    createHttpClientWith(busyPerson).execute(createAppointmentRequest);

    Account calendarViewer = getRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest = new CalDavFreeBusyRequestBuilder(davBaseUrl)
        .asICalendar()
        .originatorEmail(calendarViewer.getName())
        .recipientEmail(busyPerson.getName())
        .timeslot("20231206T114500", "20231208T154500")
        .build();
    HttpResponse freeBusyResponse = createHttpClientWith(calendarViewer).execute(freeBusyRequest);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(freeBusyResponse));
    assertTrue(readContentFrom(freeBusyResponse).contains("FREEBUSY;FBTYPE=BUSY:20231207T124500Z/20231207T144500Z"));
  }

  private void createAppointment(Account organizer, UUID calendarUUID) throws Exception {
    HttpPut request = new CalDavCreateAppointmentRequestBuilder(davBaseUrl)
        .uuid(calendarUUID)
        .organizer(organizer)
        .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);
    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
  }

  private HttpResponse deleteAppointment(Account organizer, UUID calendarUUID) throws Exception {
    HttpClient client = createHttpClientWith(organizer);
    HttpDelete request = new HttpDelete(getAccountCalendarUrl(organizer, calendarUUID));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  private HttpResponse getAppointment(Account organizer, UUID calendarUUID) throws Exception {
    HttpGet request = new HttpGet(getAccountCalendarUrl(organizer, calendarUUID));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return createHttpClientWith(organizer).execute(request);
  }

  private String getAccountCalendarUrl(Account account, UUID calendarUUID) {
    String accountEmail = URLEncoder.encode(account.getName(), StandardCharsets.UTF_8);
    return davBaseUrl + "/home/" + accountEmail + "/Calendar/" + calendarUUID + ".ics";
  }

  private HttpClient createHttpClientWith(Account account) throws Exception {
    AuthToken authToken = AuthProvider.getAuthToken(account);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }

  private static int statusCodeFrom(HttpResponse response) {
    return response.getStatusLine().getStatusCode();
  }

  private static String readContentFrom(HttpResponse response) throws IOException {
    return new String(response.getEntity().getContent().readAllBytes());
  }
}