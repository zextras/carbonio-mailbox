// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zimbra.common.calendar.CalendarUtil;
import com.zimbra.common.calendar.Geo;
import com.zimbra.common.calendar.ZCalendar.ZParameter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.zclient.ZClientException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ZInvite implements ToZJSONObject {

  private List<ZTimeZone> mTimeZones;
  private List<ZComponent> mComponents;
  private ZInviteType mType;

	public ZInvite(Element e) throws ServiceException {
    mTimeZones = new ArrayList<>();
    mType = ZInviteType.fromString(e.getAttribute(MailConstants.A_TYPE, ZInviteType.appt.name()));
    for (Element tzEl : e.listElements(MailConstants.E_CAL_TZ)) {
      mTimeZones.add(new ZTimeZone(tzEl));
    }
    mComponents = new ArrayList<>();
    for (Element compEl : e.listElements(MailConstants.E_INVITE_COMPONENT)) {
      mComponents.add(new ZComponent(compEl));
    }
  }

	public ZComponent getComponent() {
    return (mComponents == null || mComponents.isEmpty()) ? null : mComponents.get(0);
  }

	@Override
  public ObjectNode toObjectNode() {
    ObjectNode zjo = JsonNodeFactory.instance.objectNode();
    zjo.put("type", mType.name());
    ZObjectNode.put(zjo, "timezones", mTimeZones);
    ZObjectNode.put(zjo, "components", mComponents);
    return zjo;
  }

  public String toString() {
    return String.format("[ZInvite %s]", getComponent().getName());
  }

  public enum ZInviteType {
    appt,
    task;

    public static ZInviteType fromString(String s) throws ServiceException {
      try {
        return ZInviteType.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid type " + s + ", valid values: " + Arrays.asList(ZInviteType.values()), e);
      }
    }

	}

  public static class ZComponent implements ToZJSONObject {

    private ZStatus mStatus;
    private ZClass mClass;
    private ZFreeBusyStatus mFreeBusyStatus;
    private ZFreeBusyStatus mActualFreeBusyStatus;
    private ZTransparency mTransparency;
    private boolean mIsAllDay;
    private boolean mDraft;
    private String mName;
    private String mLocation;
    private List<String> mCategories;
    private List<String> mComments;
    private List<String> mContacts;
    private Geo mGeo;
    private String mUrl;
    private boolean mIsException;
    private boolean mIsOrganizer;
    private long mSequenceNumber;
    private String mPriority;
    private String mPercentCompleted;
    private String mCompleted;
    private List<ZReply> mReplies;
    private ZDateTime mStart;
    private ZDateTime mEnd;
    private ZDuration mDuration;
    private ZOrganizer mOrganizer;
    private List<ZAttendee> mAttendees;
    private ZRecurrence mRecurrence;
    private String mComponentNum;
    private List<ZAlarm> mAlarms;
    private String mRecurrenceIdZ;
    private String mDescription;
    private String mDescriptionHtml;
    private ZMethod mMethod;
    private boolean mIsNoBlob;

		public ZComponent(Element e) throws ServiceException {
      mStatus = ZStatus.fromString(e.getAttribute(MailConstants.A_CAL_STATUS, ZStatus.CONF.name()));
      mClass = ZClass.fromString(e.getAttribute(MailConstants.A_CAL_CLASS, ZClass.PUB.name()));
      mFreeBusyStatus =
          ZFreeBusyStatus.fromString(
              e.getAttribute(MailConstants.A_APPT_FREEBUSY, ZFreeBusyStatus.B.name()));
      mActualFreeBusyStatus =
          ZFreeBusyStatus.fromString(
              e.getAttribute(MailConstants.A_APPT_FREEBUSY_ACTUAL, ZFreeBusyStatus.B.name()));
      mTransparency =
          ZTransparency.fromString(e.getAttribute(MailConstants.A_APPT_TRANSPARENCY, "O"));
      mIsAllDay = e.getAttributeBool(MailConstants.A_CAL_ALLDAY, false);
      mDraft = e.getAttributeBool(MailConstants.A_CAL_DRAFT, false);
      mName = e.getAttribute(MailConstants.A_NAME, null);
      mLocation = e.getAttribute(MailConstants.A_CAL_LOCATION, null);
      mMethod =
          ZMethod.fromString(e.getAttribute(MailConstants.A_CAL_METHOD, ZMethod.PUBLISH.name()));

      Iterator<Element> catIter = e.elementIterator(MailConstants.E_CAL_CATEGORY);
      if (catIter.hasNext()) {
        List<String> categories = new ArrayList<>();
        for (; catIter.hasNext(); ) {
          String cat = catIter.next().getTextTrim();
          categories.add(cat);
        }
        mCategories = categories;
      }
      Iterator<Element> cmtIter = e.elementIterator(MailConstants.E_CAL_COMMENT);
      if (cmtIter.hasNext()) {
        List<String> comments = new ArrayList<>();
        for (; cmtIter.hasNext(); ) {
          String cmt = cmtIter.next().getTextTrim();
          comments.add(cmt);
        }
        mComments = comments;
      }
      Iterator<Element> cnIter = e.elementIterator(MailConstants.E_CAL_CONTACT);
      if (cnIter.hasNext()) {
        List<String> contacts = new ArrayList<>();
        for (; cnIter.hasNext(); ) {
          String cn = cnIter.next().getTextTrim();
          contacts.add(cn);
        }
        mContacts = contacts;
      }
      Element geoElem = e.getOptionalElement(MailConstants.E_CAL_GEO);
      if (geoElem != null) mGeo = Geo.parse(geoElem);
      mUrl = e.getAttribute(MailConstants.A_CAL_URL, null);

      mIsException = e.getAttributeBool(MailConstants.A_CAL_IS_EXCEPTION, false);
      mIsOrganizer = e.getAttributeBool(MailConstants.A_CAL_ISORG, false);
      mSequenceNumber = e.getAttributeLong(MailConstants.A_CAL_SEQUENCE, 0);
      mPriority = e.getAttribute(MailConstants.A_CAL_PRIORITY, "0");
      mComponentNum = e.getAttribute(MailConstants.A_CAL_COMPONENT_NUM, "0");
      mReplies = new ArrayList<>();
      mAlarms = new ArrayList<>();

      Element repliesEl = e.getOptionalElement(MailConstants.E_CAL_REPLIES);
      if (repliesEl != null) {
        for (Element replyEl : repliesEl.listElements(MailConstants.E_CAL_REPLY)) {
          mReplies.add(new ZReply(replyEl));
        }
      }
      Element startEl = e.getOptionalElement(MailConstants.E_CAL_START_TIME);
      if (startEl != null) mStart = new ZDateTime(startEl);

      Element endEl = e.getOptionalElement(MailConstants.E_CAL_END_TIME);
      if (endEl != null) mEnd = new ZDateTime(endEl);
      Element durEl = e.getOptionalElement(MailConstants.E_CAL_DURATION);
      if (durEl != null) mDuration = new ZDuration(durEl);
      Element orEl = e.getOptionalElement(MailConstants.E_CAL_ORGANIZER);
      if (orEl != null) mOrganizer = new ZOrganizer(orEl);
      mAttendees = new ArrayList<>();
      for (Element attendeeEl : e.listElements(MailConstants.E_CAL_ATTENDEE)) {
        mAttendees.add(new ZAttendee(attendeeEl));
      }

      mRecurrenceIdZ = e.getAttribute(MailConstants.A_CAL_RECURRENCE_ID_Z, null);

      Element recurEl = e.getOptionalElement(MailConstants.E_CAL_RECUR);
      if (recurEl != null) mRecurrence = new ZRecurrence(recurEl);
      Element alarmEl = e.getOptionalElement(MailConstants.A_CAL_ALARM);
      if (alarmEl != null) {
        for (Element alarm : e.listElements(MailConstants.A_CAL_ALARM)) {
          mAlarms.add(new ZAlarm(alarm));
        }
      }

      mIsNoBlob = e.getAttributeBool(MailConstants.A_CAL_NO_BLOB, false);

      Element descEl = e.getOptionalElement(MailConstants.E_CAL_DESCRIPTION);
      mDescription = descEl != null ? descEl.getText() : null;

      Element descHtmlElem = e.getOptionalElement(MailConstants.E_CAL_DESC_HTML);
      mDescriptionHtml = descHtmlElem != null ? descHtmlElem.getText() : null;
    }

    public ZStatus getStatus() {
      return mStatus;
    }

    public String getName() {
      return mName;
    }

		public String getLocation() {
      return mLocation;
    }

		public List<String> getCategories() {
      return mCategories;
    }

		public Geo getGeo() {
      return mGeo;
    }

		public String getPriority() {
      return mPriority;
    }

		public String getPercentCompleted() {
      return mPercentCompleted;
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      zjo.put("status", mStatus.name());
      zjo.put("class", mClass.name());
      zjo.put("freeBusyStatus", mFreeBusyStatus.name());
      zjo.put("actualFreeBusyStatus", mActualFreeBusyStatus.name());
      zjo.put("transparency", mTransparency.name());
      zjo.put("isAllDay", mIsAllDay);
      zjo.put("draft", mDraft);
      if (mName != null) zjo.put("name", mName);
      zjo.put("method", mMethod.name());
      if (mComponentNum != null) zjo.put("compNum", mComponentNum);
      if (mLocation != null) zjo.put("location", mLocation);
      ZObjectNode.putList(zjo, "categories", mCategories);
      ZObjectNode.putList(zjo, "comments", mComments);
      ZObjectNode.putList(zjo, "contacts", mContacts);
      if (mGeo != null) zjo.put("geo", mGeo.toString());
      if (mUrl != null) zjo.put("url", mUrl);
      zjo.put("isOrganizer", mIsOrganizer);
      zjo.put("sequenceNumber", mSequenceNumber);
      if (mPriority != null) zjo.put("priority", mPriority);
      if (mPercentCompleted != null) zjo.put("percentCompleted", mPercentCompleted);
      if (mCompleted != null) zjo.put("completed", mCompleted);
      ZObjectNode.put(zjo, "replies", mReplies);
      if (mStart != null) zjo.set("start", mStart.toObjectNode());
      if (mEnd != null) zjo.set("end", mEnd.toObjectNode());
      if (mDuration != null) zjo.set("duration", mDuration.toObjectNode());
      if (mOrganizer != null) zjo.set("organizer", mOrganizer.toObjectNode());
      ZObjectNode.put(zjo, "attendees", mAttendees);
      if (mRecurrence != null) zjo.set("recurrence", mRecurrence.toObjectNode());
      if (mRecurrenceIdZ != null) zjo.put("recurrenceId", mRecurrenceIdZ);
      if (mDescription != null) zjo.put("desc", mDescription);
      if (mDescriptionHtml != null) zjo.put("deschtml", mDescriptionHtml);
      zjo.put("noblob", mIsNoBlob);
      return zjo;
    }

    public String toString() {
      return String.format("[ZComponent %s]", getName());
    }

    public static class ZReply implements ToZJSONObject {

      private long mDate;
      private String mAttendee;
      private ZParticipantStatus mParticipantStatus;
      private int mRangeType;
      private String mRecurrenceId;
      private String mTimeZone;

			@Override
      public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        zjo.put("date", mDate);
        if (mAttendee != null) zjo.put("attendee", mAttendee);
        zjo.put("participantStatus", mParticipantStatus.name());
        return zjo;
      }

      public String toString() {
        return String.format("[ZReply %s]", mAttendee);
      }

      public ZReply(Element e) throws ServiceException {
        mDate = e.getAttributeLong(MailConstants.A_DATE, 0);
        mAttendee = e.getAttribute(MailConstants.A_CAL_ATTENDEE, null);
        mParticipantStatus =
            ZParticipantStatus.fromString(
                e.getAttribute(MailConstants.A_CAL_PARTSTAT, ZParticipantStatus.AC.name()));
        mRangeType = (int) e.getAttributeLong(MailConstants.A_CAL_RECURRENCE_RANGE_TYPE, 0);
        mRecurrenceId = e.getAttribute(MailConstants.A_CAL_RECURRENCE_ID, null);
        mTimeZone = e.getAttribute(MailConstants.A_CAL_TIMEZONE, null);
      }

    }
  }

  public static class ZTimeZone implements ToZJSONObject {

    private String mId;
    private long mStandardOffset;
    private long mDaylightSavingsOffset;
    private ZTransitionRule mStandard;
    private ZTransitionRule mDaylight;
    private String mStandardTzname;
    private String mDaylightTzname;

		public ZTimeZone(Element e) throws ServiceException {
      mId = e.getAttribute(MailConstants.A_ID);
      mStandardTzname = e.getAttribute(MailConstants.A_CAL_TZ_STDNAME, null);
      mDaylightTzname = e.getAttribute(MailConstants.A_CAL_TZ_DAYNAME, null);
      mStandardOffset = e.getAttributeLong(MailConstants.A_CAL_TZ_STDOFFSET, 0);
      mDaylightSavingsOffset = e.getAttributeLong(MailConstants.A_CAL_TZ_DAYOFFSET, -1);
      Element standardEl = e.getOptionalElement(MailConstants.E_CAL_TZ_STANDARD);
      if (standardEl != null) mStandard = new ZTransitionRule(standardEl);
      Element daylightEl = e.getOptionalElement(MailConstants.E_CAL_TZ_DAYLIGHT);
      if (daylightEl != null) mDaylight = new ZTransitionRule(daylightEl);
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      if (mId != null) zjo.put("id", mId);
      zjo.put("standardOffset", mStandardOffset);
      zjo.put("daylightOffset", mDaylightSavingsOffset);
      if (mStandard != null) zjo.set("standardRule", mStandard.toObjectNode());
      if (mDaylight != null) zjo.set("daylightRule", mDaylight.toObjectNode());
      if (mStandardTzname != null) zjo.put("standardTzname", mStandardTzname);
      if (mDaylightTzname != null) zjo.put("daylightTzname", mDaylightTzname);
      return zjo;
    }

    public String toString() {
      return String.format("[ZTimeZone %s]", mId);
    }

    public static class ZTransitionRule implements ToZJSONObject {
      private int mWeek;
      private int mDayOfWeek;
      private int mMonth;
      private int mDayOfMonth;
      private int mHour;
      private int mMinute;
      private int mSecond;

			public ZTransitionRule(Element e) throws ServiceException {
        mWeek = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_WEEK, 0);
        mDayOfWeek = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_DAYOFWEEK, 0);
        mDayOfMonth = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_DAYOFMONTH, 0);
        mMonth = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_MONTH, 0);
        mHour = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_HOUR, 0);
        mMinute = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_MINUTE, 0);
        mSecond = (int) e.getAttributeLong(MailConstants.A_CAL_TZ_SECOND, 0);
      }

      @Override
      public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        zjo.put("week", mWeek);
        zjo.put("dayOfWeek", mDayOfWeek);
        zjo.put("dayOfMonth", mDayOfMonth);
        zjo.put("month", mMonth);
        zjo.put("hour", mHour);
        zjo.put("minute", mMinute);
        zjo.put("second", mSecond);
        return zjo;
      }

      public String toString() {
        return "[ZTransitionRule]";
      }
    }
  }

  public static class ZDuration implements ToZJSONObject {

    private boolean mNegative;
    private int mWeeks;
    private int mDays;
    private int mHours;
    private int mMinutes;
    private int mSeconds;

		public ZDuration(Element e) throws ServiceException {
      mNegative = e.getAttributeBool(MailConstants.A_CAL_DURATION_NEGATIVE, false);
      mWeeks = (int) e.getAttributeLong(MailConstants.A_CAL_DURATION_WEEKS, 0);
      mDays = (int) e.getAttributeLong(MailConstants.A_CAL_DURATION_DAYS, 0);
      mHours = (int) e.getAttributeLong(MailConstants.A_CAL_DURATION_HOURS, 0);
      mMinutes = (int) e.getAttributeLong(MailConstants.A_CAL_DURATION_MINUTES, 0);
      mSeconds = (int) e.getAttributeLong(MailConstants.A_CAL_DURATION_SECONDS, 0);
    }

    public Element toElement(Element parent) {
      Element durEl = parent.addElement(MailConstants.E_CAL_DURATION);
      durEl.addAttribute(MailConstants.A_CAL_DURATION_NEGATIVE, mNegative);
      if (mWeeks != 0) durEl.addAttribute(MailConstants.A_CAL_DURATION_WEEKS, mWeeks);
      else {
        if (mDays != 0) durEl.addAttribute(MailConstants.A_CAL_DURATION_DAYS, mDays);
        if (mHours != 0) durEl.addAttribute(MailConstants.A_CAL_DURATION_HOURS, mHours);
        if (mMinutes != 0) durEl.addAttribute(MailConstants.A_CAL_DURATION_MINUTES, mMinutes);
        if (mSeconds != 0) durEl.addAttribute(MailConstants.A_CAL_DURATION_SECONDS, mSeconds);
      }
      return durEl;
    }

		@Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      zjo.put("negative", mNegative);
      zjo.put("weeks", mWeeks);
      zjo.put("days", mDays);
      zjo.put("hours", mHours);
      zjo.put("minutes", mMinutes);
      zjo.put("seconds", mSeconds);
      return zjo;
    }

    public String toString() {
      return "[ZDuration]"; // TODO
    }
  }

  public enum ZMethod {
    PUBLISH,
    REQUEST,
    REPLY,
    ADD,
    CANCEL,
		COUNTER,
    DECLINECOUNTER;

    public static ZMethod fromString(String s) throws ServiceException {
      try {
        return ZMethod.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid method " + s + ", valid values: " + Arrays.asList(ZMethod.values()), e);
      }
    }

  }

  public enum ZStatus {
		CONF,
    WAITING,
    DEFERRED;

    public static ZStatus fromString(String s) throws ServiceException {
      try {
        return ZStatus.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid status " + s + ", valid values: " + Arrays.asList(ZStatus.values()), e);
      }
    }

  }

  public enum ZClass {
    PUB;

    public static ZClass fromString(String s) throws ServiceException {
      try {
        return ZClass.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid class " + s + ", valid values: " + Arrays.asList(ZClass.values()), e);
      }
    }

	}

  public enum ZFreeBusyStatus {
    F,
    B,
    T,
    O;

    public static ZFreeBusyStatus fromString(String s) throws ServiceException {
      try {
        return ZFreeBusyStatus.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid free busy status "
                + s
                + ", valid values: "
                + Arrays.asList(ZFreeBusyStatus.values()),
            e);
      }
    }

	}

  public enum ZParticipantStatus {
    AC,
    DE,
    TE,
    NE,
    DG;

    public static ZParticipantStatus fromString(String s) throws ServiceException {
      try {
        return ZParticipantStatus.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid participant status "
                + s
                + ", valid values: "
                + Arrays.asList(ZParticipantStatus.values()),
            e);
      }
    }

	}

  public enum ZTransparency {
    O,
    T;

    public static ZTransparency fromString(String s) throws ServiceException {
      try {
        return ZTransparency.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid transparency "
                + s
                + ", valid values: "
                + Arrays.asList(ZTransparency.values()),
            e);
      }
    }

	}

  public enum ZRole {
		REQ,
    OPT;

    public static ZRole fromString(String s) throws ServiceException {
      try {
        return ZRole.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid role " + s + ", valid values: " + Arrays.asList(ZRole.values()), e);
      }
    }

	}

  public enum ZCalendarUserType {
    IND,
		UNK;

    public static ZCalendarUserType fromString(String s) throws ServiceException {
      try {
        return ZCalendarUserType.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid role " + s + ", valid values: " + Arrays.asList(ZCalendarUserType.values()),
            e);
      }
    }

	}

  public static class ZCalendarUser implements ToZJSONObject {
    private String mAddress;
    private String mUrl;
    private String mPersonalName;
    private String mSentBy;
    private String mDirectoryUrl;
    private String mLanguage;
    private List<ZParameter> mXParams;

    public ZCalendarUser(Element e) throws ServiceException {
      mAddress = e.getAttribute(MailConstants.A_ADDRESS, null);
      mUrl = e.getAttribute(MailConstants.A_URL, null);
      mPersonalName = e.getAttribute(MailConstants.A_DISPLAY, null);
      mSentBy = e.getAttribute(MailConstants.A_CAL_SENTBY, null);
      mDirectoryUrl = e.getAttribute(MailConstants.A_CAL_DIR, null);
      mLanguage = e.getAttribute(MailConstants.A_CAL_LANGUAGE, null);
      mXParams = CalendarUtil.parseXParams(e);
    }

    public String getAddress() {
      return mAddress;
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      if (mAddress != null) zjo.put("address", mAddress);
      if (mUrl != null) zjo.put("url", mUrl);
      if (mPersonalName != null) zjo.put("personalName", mPersonalName);
      if (mSentBy != null) zjo.put("sentBy", mSentBy);
      if (mDirectoryUrl != null) zjo.put("dir", mDirectoryUrl);
      if (mLanguage != null) zjo.put("language", mLanguage);
      return zjo;
    }

    public String toString() {
      return String.format("[ZCalendarUser %s]", mAddress);
    }
  }

  public static class ZOrganizer extends ZCalendarUser {

		public ZOrganizer(Element e) throws ServiceException {
      super(e);
    }

    public String toString() {
      return String.format("[ZOrganizer %s]", getAddress());
    }
  }

  public static class ZAttendee extends ZCalendarUser implements ToZJSONObject {
    private ZRole mRole;
    private ZParticipantStatus mParticipantStatus;
    private boolean mRSVP;
    private ZCalendarUserType mCalendarUserType;
    private String mMember;
    private String mDelegatedTo;
    private String mDelegatedFrom;

		public ZAttendee(Element e) throws ServiceException {
      super(e);
      mRole = ZRole.fromString(e.getAttribute(MailConstants.A_CAL_ROLE, ZRole.OPT.name()));
      mParticipantStatus =
          ZParticipantStatus.fromString(
              e.getAttribute(MailConstants.A_CAL_PARTSTAT, ZParticipantStatus.AC.name()));
      mRSVP = e.getAttributeBool(MailConstants.A_CAL_RSVP, false);
      mCalendarUserType =
          ZCalendarUserType.fromString(
              e.getAttribute(MailConstants.A_CAL_CUTYPE, ZCalendarUserType.IND.name()));
      mMember = e.getAttribute(MailConstants.A_CAL_MEMBER, null);
      mDelegatedTo = e.getAttribute(MailConstants.A_CAL_DELEGATED_TO, null);
      mDelegatedFrom = e.getAttribute(MailConstants.A_CAL_DELEGATED_FROM, null);
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = super.toObjectNode();
      zjo.put("role", mRole.name());
      zjo.put("participantStatus", mParticipantStatus.name());
      zjo.put("rsvp", mRSVP);
      zjo.put("calendarUserType", mCalendarUserType.name());
      if (mMember != null) zjo.put("member", mMember);
      if (mDelegatedFrom != null) zjo.put("delegatedFrom", mDelegatedFrom);
      if (mDelegatedTo != null) zjo.put("delegatedTo", mDelegatedTo);
      return zjo;
    }

    public String toString() {
      return String.format("[ZAttendee %s]", getAddress());
    }

  }

  public static class ZRecurrenceDate implements ToZJSONObject {
    private ZDateTime mStart;
    private ZDateTime mEnd;
    private ZDuration mDuration;

		public ZRecurrenceDate(Element e) throws ServiceException {
      mStart = new ZDateTime(e.getElement(MailConstants.E_CAL_START_TIME));
      Element endEl = e.getOptionalElement(MailConstants.E_CAL_END_TIME);
      if (endEl != null) mEnd = new ZDateTime(endEl);
      Element durEl = e.getOptionalElement(MailConstants.E_CAL_DURATION);
      if (durEl != null) mDuration = new ZDuration(durEl);
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      if (mStart != null) zjo.set("start", mStart.toObjectNode());
      if (mEnd != null) zjo.set("end", mEnd.toObjectNode());
      if (mDuration != null) zjo.set("duration", mDuration.toObjectNode());
      return zjo;
    }

    public String toString() {
      return "[ZRecurrenceDate]"; // TODO
    }
  }

  public static class ZRecurrenceDates implements ToZJSONObject {
    private String mTimeZoneId;
    private List<ZRecurrenceDate> mDates;

		public ZRecurrenceDates(Element e) throws ServiceException {
      mDates = new ArrayList<>();
      mTimeZoneId = e.getAttribute(MailConstants.A_CAL_TIMEZONE, null);
      for (Element dtvalEl : e.listElements(MailConstants.E_CAL_DATE_VAL)) {
        mDates.add(new ZRecurrenceDate(dtvalEl));
      }
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      if (mTimeZoneId != null) zjo.put("timeZoneId", mTimeZoneId);
      ZObjectNode.put(zjo, "dates", mDates);
      return zjo;
    }

    public String toString() {
      return "[ZRecurrenceDates]"; // TODO
    }
  }

  public enum ZFrequency {
    SEC,
		DAI,
    WEE,
    MON,
    YEA;

    public static ZFrequency fromString(String s) throws ServiceException {
      try {
        return ZFrequency.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid frequency " + s + ", valid values: " + Arrays.asList(ZFrequency.values()), e);
      }
    }

	}

  public enum ZWeekDay {
    SU,
    MO,
    TU,
    WE,
    TH,
    FR,
    SA;

    public static ZWeekDay fromString(String s) throws ServiceException {
      try {
        return ZWeekDay.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ZClientException.CLIENT_ERROR(
            "invalid weekday " + s + ", valid values: " + Arrays.asList(ZWeekDay.values()), e);
      }
    }

	}

  public enum ZByType {
    BY_SECOND,
    BY_MINUTE,
    BY_HOUR,
    BY_DAY,
    BY_MONTHDAY,
    BY_YEARDAY,
    BY_WEEKNO,
    BY_MONTH,
    BY_SETPOS;

	}

  public static class ZByDayWeekDay implements ToZJSONObject {

    private int mWeekOrd;
    private ZWeekDay mDay;

    public ZByDayWeekDay(Element e) throws ServiceException {
      mWeekOrd = (int) e.getAttributeLong(MailConstants.A_CAL_RULE_BYDAY_WKDAY_ORDWK, 0);
      mDay = ZWeekDay.fromString(e.getAttribute(MailConstants.A_CAL_RULE_DAY));
    }

    public Element toElement(Element parent) {
      Element wkdayEl = parent.addElement(MailConstants.E_CAL_RULE_BYDAY_WKDAY);
      if (mWeekOrd != 0) wkdayEl.addAttribute(MailConstants.A_CAL_RULE_BYDAY_WKDAY_ORDWK, mWeekOrd);
      wkdayEl.addAttribute(MailConstants.A_CAL_RULE_DAY, mDay.name());
      return wkdayEl;
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      zjo.put("weekOrd", mWeekOrd);
      zjo.put("day", mDay.name());
      return zjo;
    }

    public String toString() {
      return String.format("[ZByDayWeekDay weekOrd=%d day=%s]", mWeekOrd, mDay);
    }

  }

  public static class ZByRule implements ToZJSONObject {
    private ZByType mType;
    private String mList;
    private List<ZByDayWeekDay> mWeekDays;

    public ZByRule(Element e) throws ServiceException {
      String ename = e.getName();
      if (ename.equals(MailConstants.E_CAL_RULE_BYSECOND)) {

        mType = ZByType.BY_SECOND;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYSECOND_SECLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYMINUTE)) {

        mType = ZByType.BY_MINUTE;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYMINUTE_MINLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYHOUR)) {

        mType = ZByType.BY_HOUR;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYHOUR_HRLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYDAY)) {

        mType = ZByType.BY_DAY;
        mWeekDays = new ArrayList<>();
        for (Element wkdayEl : e.listElements(MailConstants.E_CAL_RULE_BYDAY_WKDAY)) {
          mWeekDays.add(new ZByDayWeekDay(wkdayEl));
        }

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYMONTHDAY)) {

        mType = ZByType.BY_MONTHDAY;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYMONTHDAY_MODAYLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYYEARDAY)) {

        mType = ZByType.BY_YEARDAY;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYYEARDAY_YRDAYLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYWEEKNO)) {

        mType = ZByType.BY_WEEKNO;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYWEEKNO_WKLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYMONTH)) {

        mType = ZByType.BY_MONTH;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYMONTH_MOLIST, null);

      } else if (ename.equals(MailConstants.E_CAL_RULE_BYSETPOS)) {

        mType = ZByType.BY_SETPOS;
        mList = e.getAttribute(MailConstants.A_CAL_RULE_BYSETPOS_POSLIST, null);
      }
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      zjo.put("type", mType.name());
      if (mList != null) zjo.put("list", mList);
      ZObjectNode.put(zjo, "weekdays", mWeekDays);
      return zjo;
    }

    public String toString() {
      return "[ZByRule]"; // TODO
    }
  }

  public static class ZRecurrenceRule implements ToZJSONObject {
    private ZFrequency mFrequency;
    private ZDateTime mUntilDate;
    private int mCount;
    private int mInterval = 1;
    private List<ZByRule> mByRules;
    private ZWeekDay mWeekStart;

    public ZRecurrenceRule(Element e) throws ServiceException {
      mFrequency = ZFrequency.fromString(e.getAttribute(MailConstants.A_CAL_RULE_FREQ));
      mByRules = new ArrayList<>();

      for (Element childEl : e.listElements()) {
        if (childEl.getName().equals(MailConstants.E_CAL_RULE_UNTIL)) {
          mUntilDate = new ZDateTime(childEl);
        } else if (childEl.getName().equals(MailConstants.E_CAL_RULE_COUNT)) {
          mCount = (int) childEl.getAttributeLong(MailConstants.A_CAL_RULE_COUNT_NUM, 1);
        } else if (childEl.getName().equals(MailConstants.E_CAL_RULE_INTERVAL)) {
          mInterval = (int) childEl.getAttributeLong(MailConstants.A_CAL_RULE_INTERVAL_IVAL, 1);
        } else if (childEl.getName().equals(MailConstants.E_CAL_RULE_WKST)) {
          mWeekStart = ZWeekDay.fromString(childEl.getAttribute(MailConstants.A_CAL_RULE_DAY));
        } else if (childEl.getName().startsWith("by")) {
          mByRules.add(new ZByRule(childEl));
        }
      }
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode zjo = JsonNodeFactory.instance.objectNode();
      zjo.put("frequency", mFrequency.name());
      if (mUntilDate != null) zjo.set("until", mUntilDate.toObjectNode());
      zjo.put("count", mCount);
      zjo.put("interval", mInterval);
      ZObjectNode.put(zjo, "byRules", mByRules);
      if (mWeekStart != null) zjo.put("weekStart", mWeekStart.name());
      return zjo;
    }

    public String toString() {
      return "[ZRecurrenceRule]"; // TODO
    }

  }

  public static class ZRecurrence implements ToZJSONObject {

    private List<ZRecurrenceRule> mRules;
    private List<ZRecurrenceDates> mDates;

    private List<ZRecurrenceRule> mExRules;
    private List<ZRecurrenceDates> mExDates;

    public ZRecurrence(Element e) throws ServiceException {
      mRules = new ArrayList<>();
      mExRules = new ArrayList<>();
      mDates = new ArrayList<>();
      mExDates = new ArrayList<>();

      for (Element addEl : e.listElements(MailConstants.E_CAL_ADD)) {
        for (Element ruleEl : addEl.listElements(MailConstants.E_CAL_RULE))
          mRules.add(new ZRecurrenceRule(ruleEl));

        for (Element datesEl : addEl.listElements(MailConstants.E_CAL_DATES))
          mDates.add(new ZRecurrenceDates(datesEl));
      }

      for (Element excludeEl : e.listElements(MailConstants.E_CAL_EXCLUDE)) {
        for (Element ruleEl : excludeEl.listElements(MailConstants.E_CAL_RULE))
          mExRules.add(new ZRecurrenceRule(ruleEl));

        for (Element datesEl : excludeEl.listElements(MailConstants.E_CAL_DATES))
          mExDates.add(new ZRecurrenceDates(datesEl));
      }
    }

    @Override
    public ObjectNode toObjectNode() {
      ObjectNode jso = JsonNodeFactory.instance.objectNode();
      ZObjectNode.put(jso, "rules", mRules);
      ZObjectNode.put(jso, "dates", mDates);
      ZObjectNode.put(jso, "exRules", mExRules);
      ZObjectNode.put(jso, "exDates", mExDates);
      return jso;
    }

    public String toString() {
      return "[ZRecurrence]";
    }
  }
}
