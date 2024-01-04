package com.zimbra.cs.dav;

import static java.lang.String.format;

import com.zimbra.common.service.ServiceException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

public class CalDavFreeBusyRequestBuilder {
  private UUID uuid = UUID.randomUUID();
  private String originatorEmail = "originator@test.com";
  private String recipientEmail = "recipient@test.com";
  private String start = "20231206T114500";
  private String end = "20231208T154500";
  private Mode mode = Mode.THUNDERBIRD;
  private final String baseUrl;

  public CalDavFreeBusyRequestBuilder(String baseUrl) throws ServiceException {
    this.baseUrl = baseUrl;
  }

  public CalDavFreeBusyRequestBuilder uuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public CalDavFreeBusyRequestBuilder originatorEmail(String value) {
    this.originatorEmail = value;
    return this;
  }

  public CalDavFreeBusyRequestBuilder recipientEmail(String value) {
    this.recipientEmail = value;
    return this;
  }

  public CalDavFreeBusyRequestBuilder timeslot(String start, String end) {
    this.start = start;
    this.end = end;
    return this;
  }

  public CalDavFreeBusyRequestBuilder asThunderbird() {
    this.mode = Mode.THUNDERBIRD;
    return this;
  }

  public CalDavFreeBusyRequestBuilder asICalendar() {
    this.mode = Mode.ICALENDAR;
    return this;
  }

  public HttpPost build() throws UnsupportedEncodingException {
    HttpPost request = new HttpPost(buildUrl());
    request.setEntity(new StringEntity(buildBody()));
    request.setHeader(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8");

    if (mode == Mode.THUNDERBIRD) {
      request.setHeader(DavProtocol.HEADER_ORIGINATOR, "mailto:" + originatorEmail);
      request.setHeader(DavProtocol.HEADER_RECIPIENT, "mailto:" + recipientEmail);
    }

    return request;
  }

  private String buildUrl() {
    String name = URLEncoder.encode(originatorEmail, StandardCharsets.UTF_8);
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
        + format("ORGANIZER:mailto:%s\n", originatorEmail)
        + originatorAsAttendee()
        + format("ATTENDEE:mailto:%s\n", recipientEmail)
        + "END:VFREEBUSY\n"
        + "END:VCALENDAR";
  }

  private String originatorAsAttendee() {
    if (mode == Mode.ICALENDAR) {
      return format("ATTENDEE:mailto:%s\n", originatorEmail);
    }
    return "";
  }

  private enum Mode {
    THUNDERBIRD,
    ICALENDAR
  }
}
