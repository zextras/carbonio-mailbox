package com.zimbra.cs.service.mail;

import com.zimbra.cs.account.Account;

public class AppointmentData {

  public final String eventTitle;
  public final Account organiser;
  public final Account attendee;
  public final String timezone;
  public final String startTime;
  public final String endTime;
  public final String location;
  public final String desc;
  public final String htmlDesc;

  private AppointmentData(AppointmentDataBuilder builder) {
    this.eventTitle = builder.eventTitle;
    this.organiser = builder.organiser;
    this.attendee = builder.attendee;
    this.timezone = builder.timezone;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.location = builder.location;
    this.desc = builder.desc;
    this.htmlDesc = builder.htmlDesc;
  }

  public AppointmentDataBuilder toBuilder() {
    return AppointmentData.builder()
        .withEventTitle(this.eventTitle)
        .withOrganiser(this.organiser)
        .withAttendee(this.attendee)
        .withTimezone(this.timezone)
        .withStartTime(this.startTime)
        .withEndTime(this.endTime)
        .withLocation(this.location)
        .withDesc(this.desc)
        .withHtmlDesc(this.htmlDesc);
  }

  public static AppointmentDataBuilder builder() {
    return new AppointmentDataBuilder();
  }

  public static final class AppointmentDataBuilder {
    private String eventTitle;
    private Account organiser;
    private Account attendee;
    private String timezone;
    private String startTime;
    private String endTime;
    private String location;
    private String desc;
    private String htmlDesc;

    private AppointmentDataBuilder() {}

    public AppointmentDataBuilder withEventTitle(String eventTitle) {
      this.eventTitle = eventTitle;
      return this;
    }

    public AppointmentDataBuilder withOrganiser(Account organiser) {
      this.organiser = organiser;
      return this;
    }

    public AppointmentDataBuilder withAttendee(Account attendee) {
      this.attendee = attendee;
      return this;
    }

    public AppointmentDataBuilder withTimezone(String timezone) {
      this.timezone = timezone;
      return this;
    }

    public AppointmentDataBuilder withStartTime(String startTime) {
      this.startTime = startTime;
      return this;
    }

    public AppointmentDataBuilder withEndTime(String endTime) {
      this.endTime = endTime;
      return this;
    }

    public AppointmentDataBuilder withLocation(String location) {
      this.location = location;
      return this;
    }

    public AppointmentDataBuilder withDesc(String desc) {
      this.desc = desc;
      return this;
    }

    public AppointmentDataBuilder withHtmlDesc(String htmlDesc) {
      this.htmlDesc = htmlDesc;
      return this;
    }

    public AppointmentData build() {
      return new AppointmentData(this);
    }
  }
}
