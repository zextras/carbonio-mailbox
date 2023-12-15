package com.zimbra.cs.dav;

import static java.lang.String.format;

import com.zimbra.common.calendar.ZCalendar;
import com.zimbra.cs.account.Account;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

public class CalDavCreateAppointmentRequestBuilder {
  private UUID uuid = UUID.randomUUID();
  private String organizer = "test@test.com";
  private ZCalendar.ScheduleAgent scheduleAgent;
  private List<Account> attendees = new ArrayList<>();
  private String start = "20231207T124500";
  private String end = "20231207T144500";
  private final String baseUrl;

  public CalDavCreateAppointmentRequestBuilder(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public CalDavCreateAppointmentRequestBuilder uuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public CalDavCreateAppointmentRequestBuilder organizer(String value) {
    this.organizer = value;
    return this;
  }

  public CalDavCreateAppointmentRequestBuilder organizer(Account value) {
    return this.organizer(value.getName());
  }

  public CalDavCreateAppointmentRequestBuilder timeslot(String start, String end) {
    this.start = start;
    this.end = end;
    return this;
  }

  public CalDavCreateAppointmentRequestBuilder addAttendee(Account attendee) {
    attendees.add(attendee);
    return this;
  }

  public CalDavCreateAppointmentRequestBuilder scheduleAgent(ZCalendar.ScheduleAgent value) {
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
