// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav.service;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.ZCalendar.ScheduleAgent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.AuthProvider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
class DavServletTest {

  private static Provisioning provisioning;
  private static Account organizer;
  private static Server server;
  private static final int PORT = 8090;
  private static final String DAV_BASE_PATH = "/dav";
  private static final String DAV_BASE_URL = "http://localhost:" + PORT + DAV_BASE_PATH;
  private static final String CALENDAR_UID = "95a5527e-df0a-4df2-b64a-7eee8e647efe";
  private static GreenMail greenMail;

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

  @Test
  void shouldNotSendNotificationWhenScheduleAgentClient() throws Exception {
    Account organizer = provisioning.getAccount("alias@test.com");
    Account attendee1 = MailboxTestUtil.createRandomAccountForDefaultDomain();
    Account attendee2 = MailboxTestUtil.createRandomAccountForDefaultDomain();
    Account attendee3 = MailboxTestUtil.createRandomAccountForDefaultDomain();
    final HttpPut request =
        new CreateAppointmentRequestBuilder(DAV_BASE_URL)
            .organizer(organizer)
            .scheduleAgent(ScheduleAgent.CLIENT)
            .addAttendee(attendee1)
            .addAttendee(attendee2)
            .addAttendee(attendee3)
            .build();
    HttpResponse response = createHttpClientWith(organizer).execute(request);

    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(response));
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  /**
   * Added for bug CO-839 (create appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldCreateAppointmentUsingCalDAV() throws Exception {
    HttpResponse createResponse = createAppointmentWithCalDAV(CALENDAR_UID + ".ics");
    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(createResponse));
    HttpResponse appointmentWithCalDAV = getAppointmentWithCalDAV();
    assertEquals(HttpStatus.SC_OK, statusCodeFrom(appointmentWithCalDAV));
    String createdAppointment = readContentFrom(getAppointmentWithCalDAV());
    assertTrue(createdAppointment.contains(CALENDAR_UID));
  }

  /**
   * Added for bug CO-840 (delete appointment with CalDAV)
   *
   * @throws Exception
   */
  @Test
  void shouldDeleteAppointmentUsingCalDAV() throws Exception {
    assertEquals(
        HttpStatus.SC_CREATED, statusCodeFrom(createAppointmentWithCalDAV(CALENDAR_UID + ".ics")));
    assertEquals(HttpStatus.SC_OK, statusCodeFrom(getAppointmentWithCalDAV()));
    assertEquals(HttpStatus.SC_NO_CONTENT, statusCodeFrom(deleteAppointmentWithCalDAV()));
    assertEquals(HttpStatus.SC_NOT_FOUND, statusCodeFrom(getAppointmentWithCalDAV()));
  }

  /**
   * Added for bug CO-823: request freebusy of addAttendee
   *
   * @throws Exception
   */
  @Test
  void createAnAppointmentAndFindThatSlotAsBusyStatus() throws Exception {
    Account busyPerson = MailboxTestUtil.createRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest =
        new CreateAppointmentRequestBuilder(DAV_BASE_URL)
            .organizer(busyPerson)
            .addAttendee(busyPerson)
            .timeslot("20231207T124500", "20231207T144500")
            .build();
    HttpResponse createAppointmentResponse =
        createHttpClientWith(busyPerson).execute(createAppointmentRequest);
    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(createAppointmentResponse));

    UUID calendarId = UUID.randomUUID();
    Account calendarViewer = MailboxTestUtil.createRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest =
        new FreeBusyRequestBuilder(DAV_BASE_URL)
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

  /**
   * Added for bug CO-860: FreeBusy status request without recipient and originator http headers
   * fails to return FB status
   *
   * <p>For example: Apple's iCalendar do not send Recipients header in the FreeBusy status request
   *
   * @throws Exception exception during making requests
   */
  @Test
  void createAnAppointmentAndFindThatSlotAsBusyStatusUsingICalendar() throws Exception {
    Account busyPerson = MailboxTestUtil.createRandomAccountForDefaultDomain();
    HttpPut createAppointmentRequest =
        new CreateAppointmentRequestBuilder(DAV_BASE_URL)
            .organizer(busyPerson)
            .addAttendee(busyPerson)
            .timeslot("20231207T124500", "20231207T144500")
            .build();
    HttpResponse createAppointmentResponse =
        createHttpClientWith(busyPerson).execute(createAppointmentRequest);
    assertEquals(HttpStatus.SC_CREATED, statusCodeFrom(createAppointmentResponse));

    Account calendarViewer = MailboxTestUtil.createRandomAccountForDefaultDomain();
    HttpPost freeBusyRequest =
        new FreeBusyRequestBuilder(DAV_BASE_URL)
            .asICalendar()
            .originator(calendarViewer)
            .recipient(busyPerson)
            .timeslot("20231206T114500", "20231208T154500")
            .build();
    HttpResponse freeBusyResponse = createHttpClientWith(calendarViewer).execute(freeBusyRequest);

    assertEquals(HttpStatus.SC_OK, statusCodeFrom(freeBusyResponse));
    assertTrue(
        readContentFrom(freeBusyResponse)
            .contains("FREEBUSY;FBTYPE=BUSY:20231207T124500Z/20231207T144500Z"));
  }

  private HttpResponse createInviteWithDavRequest(Account organizer) throws Exception {
    String url =
        "http://localhost:"
            + PORT
            + DAV_BASE_PATH
            + "/home/"
            + URLEncoder.encode(organizer.getName(), StandardCharsets.UTF_8)
            + "/Calendar/95a5527e-df0a-4df2-b64a-7eee8e647efe.ics";
    HttpPut request = new HttpPut(url);
    request.setEntity(
        new InputStreamEntity(
            Objects.requireNonNull(
                this.getClass().getResourceAsStream("Invite_ScheduleAgent_Client.ics"))));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    final HttpClient client = createHttpClientWith(organizer);
    return client.execute(request);
  }

  private HttpResponse createAppointmentWithCalDAV(String resourceFileName) throws Exception {
    HttpClient client = createHttpClientWith(organizer);
    HttpPut request = new HttpPut(getCalDavResourceUrl(organizer, DavServletTest.CALENDAR_UID));
    request.setEntity(
        new InputStreamEntity(
            Objects.requireNonNull(this.getClass().getResourceAsStream(resourceFileName))));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  private HttpResponse deleteAppointmentWithCalDAV() throws Exception {
    HttpClient client = createHttpClientWith(organizer);
    HttpDelete request =
        new HttpDelete(getCalDavResourceUrl(organizer, DavServletTest.CALENDAR_UID));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  private HttpResponse getAppointmentWithCalDAV() throws Exception {
    HttpClient client = createHttpClientWith(organizer);
    HttpGet request = new HttpGet(getCalDavResourceUrl(organizer, DavServletTest.CALENDAR_UID));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return client.execute(request);
  }

  /**
   * Returns CalDav Resource URL for this test suite {@link #organizer} and calendar {@link
   * #CALENDAR_UID}
   *
   * @param account
   * @param calendarUUID
   * @return url endpoint to make the request
   */
  private String getCalDavResourceUrl(Account account, String calendarUUID) {
    return "http://localhost:"
        + PORT
        + DAV_BASE_PATH
        + "/home/"
        + URLEncoder.encode(account.getName(), StandardCharsets.UTF_8)
        + "/Calendar/"
        + calendarUUID
        + ".ics";
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
  private Account organizer = MailboxTestUtil.createRandomAccountForDefaultDomain();
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

  public CreateAppointmentRequestBuilder organizer(Account value) {
    this.organizer = value;
    return this;
  }

  public CreateAppointmentRequestBuilder timeslot(String start, String end) {
    this.start = start;
    this.end = end;
    return this;
  }

  public HttpPut build() throws UnsupportedEncodingException {
    HttpPut createAppointmentRequest = new HttpPut(buildUrl());
    createAppointmentRequest.setEntity(new StringEntity(buildBody()));
    createAppointmentRequest.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");
    return createAppointmentRequest;
  }

  private String buildUrl() {
    String name = URLEncoder.encode(organizer.getName(), StandardCharsets.UTF_8);
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
        + "UID:"
        + uuid.toString()
        + "\n"
        + "SUMMARY:Test\n"
        + "ORGANIZER:mailto:"
        + organizer.getName()
        + "\n"
        + buildAttendees()
        + "\n"
        + "DTSTART:"
        + start
        + "\n"
        + "DTEND:"
        + end
        + "\n"
        + "TRANSP:OPAQUE\n"
        + "DESCRIPTION;ALTREP=\"data:text/html,%3Cbody%3ETest%3C%2Fbody%3E\":Test\n"
        + "END:VEVENT\n"
        + "END:VCALENDAR";
  }

  private String buildAttendees() {
    return attendees.stream()
        .map(
            attendee -> {
              if (scheduleAgent == null) {
                return "ATTENDEE:mailto:" + attendee.getName();
              }
              return "ATTENDEE;SCHEDULE-AGENT=" + scheduleAgent + ":mailto:" + attendee.getName();
            })
        .collect(Collectors.joining("\n"));
  }

  public CreateAppointmentRequestBuilder addAttendee(Account attendee) {
    attendees.add(attendee);
    return this;
  }

  public CreateAppointmentRequestBuilder scheduleAgent(ScheduleAgent value) {
    scheduleAgent = value;
    return this;
  }
}

class FreeBusyRequestBuilder {
  private UUID uuid = UUID.randomUUID();
  private Account originator = MailboxTestUtil.createRandomAccountForDefaultDomain();
  private Account recipient = MailboxTestUtil.createRandomAccountForDefaultDomain();
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
        + "UID:"
        + uuid
        + "\n"
        + "DTSTAMP:20231107T113758Z\n"
        + "DTSTART:"
        + start
        + "\n"
        + "DTEND:"
        + end
        + "\n"
        + "ORGANIZER:mailto:"
        + originator.getName()
        + "\n"
        + ((mode == Mode.ICALENDAR) ? ("ATTENDEE:mailto:" + originator.getName() + "\n") : "")
        + "ATTENDEE:mailto:"
        + recipient.getName()
        + "\n"
        + "END:VFREEBUSY\n"
        + "END:VCALENDAR";
  }

  private enum Mode {
    THUNDERBIRD,
    ICALENDAR
  }
}
