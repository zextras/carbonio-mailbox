// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.AuthProvider;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zextras.mailbox.util.MailboxTestUtil.DEFAULT_DOMAIN;
import static com.zextras.mailbox.util.MailboxTestUtil.createRandomAccountForDefaultDomain;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("api")
class DavServletTest {

  private static Server server;
  private static final int PORT = 8090;
  private static final String DAV_BASE_PATH = "/dav";
  private static final String DAV_BASE_URL = "http://localhost:" + PORT + DAV_BASE_PATH;
  private static GreenMail greenMail;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    greenMail =
        new GreenMail(
            new ServerSetup[]{
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    server = JettyServerFactory.create(PORT, Map.of(DAV_BASE_PATH + "/*", new ServletHolder(DavServlet.class)));
    server.start();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    server.stop();
    greenMail.stop();
    MailboxTestUtil.tearDown();
  }

  @BeforeEach
  void beforeEach() {
    greenMail.reset();
  }

  @Test
  void shouldNotSendNotificationWhenScheduleAgentClient() throws Exception {
    Account organizer = createRandomAccountForDefaultDomain();
    organizer.addAlias("alias@" + DEFAULT_DOMAIN);

    final HttpPut request = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
        .organizer("alias@" + DEFAULT_DOMAIN)
        .scheduleAgent(ScheduleAgent.CLIENT)
        .addAttendee(createRandomAccountForDefaultDomain())
        .addAttendee(createRandomAccountForDefaultDomain())
        .addAttendee(createRandomAccountForDefaultDomain())
        .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @Test
  void shouldSendNotificationsForEachAttendeeWhenScheduleAgentIsServer() throws Exception {
    Account organizer = createRandomAccountForDefaultDomain();

    final HttpPut request = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
        .organizer(organizer)
        .scheduleAgent(ScheduleAgent.SERVER)
        .addAttendee(createRandomAccountForDefaultDomain())
        .addAttendee(createRandomAccountForDefaultDomain())
        .addAttendee(createRandomAccountForDefaultDomain())
        .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
    assertEquals(3, greenMail.getReceivedMessages().length);
  }

  /**
   * Added for bug CO-839 (create appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldCreateAppointment() throws Exception {
    Account organizer = createRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    HttpPut createAppointmentRequest = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
        .uuid(calendarUUID)
        .organizer(organizer)
        .build();

    HttpResponse createAppointmentResponse = createHttpClientWith(organizer).execute(createAppointmentRequest);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(createAppointmentResponse));
  }

  @Test
  void shouldGetACreatedAppointment() throws Exception {
    Account organizer = createRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    createAppointment(organizer, calendarUUID);

    HttpResponse getAppointmentResponse = getAppointment(organizer, calendarUUID);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(getAppointmentResponse));
    assertTrue(readContentFrom(getAppointmentResponse).contains("UID:" + calendarUUID));
  }

  /**
   * Added for bug CO-840 (delete appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldDeleteAppointment() throws Exception {
    Account organizer = createRandomAccountForDefaultDomain();
    UUID calendarUUID = UUID.randomUUID();
    createAppointment(organizer, calendarUUID);

    HttpResponse deleteAppointmentResponse = deleteAppointment(organizer, calendarUUID);

    assertEquals(HttpStatus.SC_NO_CONTENT, statusCodeFrom(deleteAppointmentResponse));
    assertEquals(HttpStatus.SC_NOT_FOUND, statusCodeFrom(getAppointment(organizer, calendarUUID)));
  }

  /**
   * Added for bug CO-823: request freebusy of addAttendee
   *
   * @throws Exception
   */
  @Test
  void createAnAppointmentAndFindThatSlotAsBusyStatus() throws Exception {
    Account busyPerson = createRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
        .organizer(busyPerson)
        .addAttendee(busyPerson)
        .timeslot("20231207T124500", "20231207T144500")
        .build();
    createHttpClientWith(busyPerson).execute(createAppointmentRequest);

    UUID calendarId = UUID.randomUUID();
    Account calendarViewer = createRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest = new FreeBusyRequestBuilder(DAV_BASE_URL)
        .asThunderbird()
        .uuid(calendarId)
        .originator(calendarViewer)
        .recipient(busyPerson)
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
    Account busyPerson = createRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
        .organizer(busyPerson)
        .addAttendee(busyPerson)
        .timeslot("20231207T124500", "20231207T144500")
        .build();
    createHttpClientWith(busyPerson).execute(createAppointmentRequest);

    Account calendarViewer = createRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest = new FreeBusyRequestBuilder(DAV_BASE_URL)
        .asICalendar()
        .originator(calendarViewer)
        .recipient(busyPerson)
        .timeslot("20231206T114500", "20231208T154500")
        .build();
    HttpResponse freeBusyResponse = createHttpClientWith(calendarViewer).execute(freeBusyRequest);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(freeBusyResponse));
    assertTrue(readContentFrom(freeBusyResponse).contains("FREEBUSY;FBTYPE=BUSY:20231207T124500Z/20231207T144500Z"));
  }

  private void createAppointment(Account organizer, UUID calendarUUID) throws Exception {
    HttpPut request = new CreateAppointmentRequestBuilder(DAV_BASE_URL)
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
    return DAV_BASE_URL + "/home/" + accountEmail + "/Calendar/" + calendarUUID + ".ics";
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

class CreateAppointmentRequestBuilder {
  private UUID uuid = UUID.randomUUID();
  private String organizer = createRandomAccountForDefaultDomain().getName();
  private ScheduleAgent scheduleAgent;
  private List<Account> attendees = new ArrayList<>();
  private String start = "20231207T124500";
  private String end = "20231207T144500";
  private final String baseUrl;

  CreateAppointmentRequestBuilder(String baseUrl) throws ServiceException {
    this.baseUrl = baseUrl;
  }

  public CreateAppointmentRequestBuilder uuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public CreateAppointmentRequestBuilder organizer(String value) {
    this.organizer = value;
    return this;
  }

  public CreateAppointmentRequestBuilder organizer(Account value) {
    return this.organizer(value.getName());
  }

  public CreateAppointmentRequestBuilder timeslot(String start, String end) {
    this.start = start;
    this.end = end;
    return this;
  }

  public CreateAppointmentRequestBuilder addAttendee(Account attendee) {
    attendees.add(attendee);
    return this;
  }

  public CreateAppointmentRequestBuilder scheduleAgent(ScheduleAgent value) {
    scheduleAgent = value;
    return this;
  }

  public HttpPut build() throws UnsupportedEncodingException {
    HttpPut createAppointmentRequest = new HttpPut(buildUrl());
    createAppointmentRequest.setEntity(new StringEntity(buildBody()));
    createAppointmentRequest.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return createAppointmentRequest;
  }

  private String buildUrl() {
    String name = URLEncoder.encode(organizer, StandardCharsets.UTF_8);
    return baseUrl + "/home/" + name + "/Calendar/" + uuid.toString() + ".ics";
  }

  private String buildBody() {
    return "BEGIN:VCALENDAR\n"
        + "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n"
        + "VERSION:2.0\n"
        + "BEGIN:VTIMEZONE\n"
        + "TZID:Europe/Rome\n"
        + "BEGIN:STANDARD\n"
        + "DTSTART:16010101T000000\n"
        + "TZOFFSETTO:+0530\n"
        + "TZOFFSETFROM:+0530\n"
        + "TZNAME:IST\n"
        + "END:STANDARD\n"
        + "END:VTIMEZONE\n"
        + "BEGIN:VEVENT\n"
        + "CREATED:20230918T125911Z\n"
        + "LAST-MODIFIED:20230918T130107Z\n"
        + "DTSTAMP:20230918T130107Z\n"
        + format("UID:%s\n", uuid)
        + "SUMMARY:Test\n"
        + format("ORGANIZER:mailto:%s\n", organizer)
        + buildAttendees()
        + format("DTSTART:%s\n", start)
        + format("DTEND:%s\n", end)
        + "TRANSP:OPAQUE\n"
        + "DESCRIPTION;ALTREP=\"data:text/html,%3Cbody%3ETest%3C%2Fbody%3E\":Test\n"
        + "END:VEVENT\n"
        + "END:VCALENDAR";
  }

  private String buildAttendees() {
    return attendees.stream().map(attendee -> {
      if (scheduleAgent == null) {
        return "ATTENDEE:mailto:" + attendee.getName();
      }
      return "ATTENDEE;SCHEDULE-AGENT=" + scheduleAgent + ":mailto:" + attendee.getName();
    }).collect(Collectors.joining("\n")) + "\n";
  }
}

class FreeBusyRequestBuilder {
  private UUID uuid = UUID.randomUUID();
  private Account originator = createRandomAccountForDefaultDomain();
  private Account recipient = createRandomAccountForDefaultDomain();
  private String start = "20231206T114500";
  private String end = "20231208T154500";
  private Mode mode = Mode.THUNDERBIRD;
  private final String baseUrl;

  FreeBusyRequestBuilder(String baseUrl) throws ServiceException {
    this.baseUrl = baseUrl;
  }

  public FreeBusyRequestBuilder uuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public FreeBusyRequestBuilder originator(Account value) {
    this.originator = value;
    return this;
  }

  public FreeBusyRequestBuilder recipient(Account value) {
    this.recipient = value;
    return this;
  }

  public FreeBusyRequestBuilder timeslot(String start, String end) {
    this.start = start;
    this.end = end;
    return this;
  }

  public FreeBusyRequestBuilder asThunderbird() {
    this.mode = Mode.THUNDERBIRD;
    return this;
  }

  public FreeBusyRequestBuilder asICalendar() {
    this.mode = Mode.ICALENDAR;
    return this;
  }

  public HttpPost build() throws UnsupportedEncodingException {
    HttpPost request = new HttpPost(buildUrl());
    request.setEntity(new StringEntity(buildBody()));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");

    if (mode == Mode.THUNDERBIRD) {
      request.setHeader(DavProtocol.HEADER_ORIGINATOR, "mailto:" + originator.getName());
      request.setHeader(DavProtocol.HEADER_RECIPIENT, "mailto:" + recipient.getName());
    }

    return request;
  }

  private String buildUrl() {
    String name = URLEncoder.encode(originator.getName(), StandardCharsets.UTF_8);
    return baseUrl + "/home/" + name + "/Sent";
  }

  private String buildBody() {
    return "BEGIN:VCALENDAR\n"
        + "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n"
        + "VERSION:2.0\n"
        + "METHOD:REQUEST\n"
        + "BEGIN:VFREEBUSY\n"
        + "TZID:Europe/Rome\n"
        + format("UID:%s\n", uuid)
        + "DTSTAMP:20231107T113758Z\n"
        + format("DTSTART:%s\n", start)
        + format("DTEND:%s\n", end)
        + format("ORGANIZER:mailto:%s\n", originator.getName())
        + originatorAsAttendee()
        + format("ATTENDEE:mailto:%s\n", recipient.getName())
        + "END:VFREEBUSY\n"
        + "END:VCALENDAR";
  }

  private String originatorAsAttendee() {
    if (mode == Mode.ICALENDAR) {
      return format("ATTENDEE:mailto:%s\n", originator.getName());
    }
    return "";
  }

  private enum Mode {
    THUNDERBIRD,
    ICALENDAR
  }
}
