// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.AlarmInfoInterface;
import com.zimbra.soap.base.CalOrganizerInterface;
import com.zimbra.soap.base.CalendarAttendeeInterface;
import com.zimbra.soap.base.DtTimeInfoInterface;
import com.zimbra.soap.base.DurationInfoInterface;
import com.zimbra.soap.base.ExceptionRecurIdInfoInterface;
import com.zimbra.soap.base.GeoInfoInterface;
import com.zimbra.soap.base.InviteComponentInterface;
import com.zimbra.soap.base.RecurrenceInfoInterface;
import com.zimbra.soap.base.XPropInterface;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "categories", "comments", "contacts", "geo",
    "attendees", "alarms", "xProps", "fragment",
    "description", "htmlDescription", "organizer",
    "recurrence", "exceptionId", "dtStart", "dtEnd", "duration" })
public class InviteComponent
extends InviteComponentCommon
implements InviteComponentInterface
{
    // {@link InviteComponent} and {@link InviteComponentWithGroupInfo} only
    // differ in the object representing E_CAL_ATTENDEE

    /**
     * @zm-api-field-tag invite-comp-category
     * @zm-api-field-description Categories - for iCalendar CATEGORY properties
     */
    @XmlElement(name=MailConstants.E_CAL_CATEGORY /* category */, required=false)
    private final List<String> categories = Lists.newArrayList();

    /**
     * @zm-api-field-tag invite-comp-comment
     * @zm-api-field-description Comments - for iCalendar COMMENT properties
     */
    @XmlElement(name=MailConstants.E_CAL_COMMENT /* comment */, required=false)
    private final List<String> comments = Lists.newArrayList();

    /**
     * @zm-api-field-tag invite-comp-contact
     * @zm-api-field-description Contacts - for iCalendar CONTACT properties
     */
    @XmlElement(name=MailConstants.E_CAL_CONTACT /* contact */, required=false)
    private final List<String> contacts = Lists.newArrayList();

    /**
     * @zm-api-field-description for iCalendar GEO property
     */
    @XmlElement(name=MailConstants.E_CAL_GEO /* geo */, required=false)
    private GeoInfo geo;

    /**
     * @zm-api-field-description Attendees
     */
    @XmlElement(name=MailConstants.E_CAL_ATTENDEE /* at */, required=false)
    private final List<CalendarAttendee> attendees = Lists.newArrayList();

    /**
     * @zm-api-field-description Alarm information
     */
    @XmlElement(name=MailConstants.E_CAL_ALARM /* alarm */, required=false)
    private final List<AlarmInfo> alarms = Lists.newArrayList();

    /**
     * @zm-api-field-description iCalender XPROP properties
     */
    @XmlElement(name=MailConstants.E_CAL_XPROP /* xprop */, required=false)
    private final List<XProp> xProps = Lists.newArrayList();

    /**
     * @zm-api-field-tag invite-comp-fragment
     * @zm-api-field-description First few bytes of the message (probably between 40 and 100 bytes)
     */
    @ZimbraJsonAttribute
    @XmlElement(name=MailConstants.E_FRAG /* fr */, required=false)
    private String fragment;

    /**
     * @zm-api-field-tag invite-comp-desc
     * @zm-api-field-description Present if noBlob is set and invite has a plain text description
     */
    @XmlElement(name=MailConstants.E_CAL_DESCRIPTION /* desc */, required=false)
    private String description;

    /**
     * @zm-api-field-tag invite-comp-html-desc
     * @zm-api-field-description Present if noBlob is set and invite has an HTML description
     */
    @XmlElement(name=MailConstants.E_CAL_DESC_HTML /* descHtml */, required=false)
    private String htmlDescription;

    /**
     * @zm-api-field-description Organizer
     */
    @XmlElement(name=MailConstants.E_CAL_ORGANIZER /* or */, required=false)
    private CalOrganizer organizer;

    /**
     * @zm-api-field-description Recurrence information
     */
    @XmlElement(name=MailConstants.E_CAL_RECUR /* recur */, required=false)
    private RecurrenceInfo recurrence;

    /**
     * @zm-api-field-description RECURRENCE-ID, if this is an exception
     */
    @XmlElement(name=MailConstants.E_CAL_EXCEPTION_ID /* exceptId */, required=false)
    private ExceptionRecurIdInfo exceptionId;

    // For JSON, wrapped in array because ToXML.encodeDtStart used addElement instead of addUniqueElement :-(
    /**
     * @zm-api-field-description Start date-time (required)
     */
    @XmlElement(name=MailConstants.E_CAL_START_TIME /* s */, required=false)
    private DtTimeInfo dtStart;

    // For JSON, wrapped in array because ToXML.encodeDtEnd used addElement instead of addUniqueElement :-(
    /**
     * @zm-api-field-description End date-time
     */
    @XmlElement(name=MailConstants.E_CAL_END_TIME /* e */, required=false)
    private DtTimeInfo dtEnd;

    /**
     * @zm-api-field-description Duration
     */
    @XmlElement(name=MailConstants.E_CAL_DURATION /* dur */, required=false)
    private DurationInfo duration;

    public InviteComponent() {
    }

    public InviteComponent(String method, int componentNum, boolean rsvp) {
        super(method, componentNum, rsvp);
    }

    @Override
    public InviteComponentInterface createFromMethodComponentNumRsvp(
            String method, int componentNum, boolean rsvp) {
        return new InviteComponent(method, componentNum, rsvp);
    }

    @Override
    public void setCategories(Iterable <String> categories) {
        this.categories.clear();
        if (categories != null) {
            Iterables.addAll(this.categories,categories);
        }
    }

    @Override
    public void addCategory(String category) {
        this.categories.add(category);
    }

    @Override
    public void setComments(Iterable <String> comments) {
        this.comments.clear();
        if (comments != null) {
            Iterables.addAll(this.comments,comments);
        }
    }

    @Override
    public void addComment(String comment) {
        this.comments.add(comment);
    }

    @Override
    public void setContacts(Iterable <String> contacts) {
        this.contacts.clear();
        if (contacts != null) {
            Iterables.addAll(this.contacts,contacts);
        }
    }

    @Override
    public void addContact(String contact) {
        this.contacts.add(contact);
    }
    public void setGeo(GeoInfo geo) { this.geo = geo; }
    public void setAttendees(Iterable <CalendarAttendee> attendees) {
        this.attendees.clear();
        if (attendees != null) {
            Iterables.addAll(this.attendees,attendees);
        }
    }

    public void addAttendee(CalendarAttendee attendee) {
        this.attendees.add(attendee);
    }

    public void setAlarms(Iterable <AlarmInfo> alarms) {
        this.alarms.clear();
        if (alarms != null) {
            Iterables.addAll(this.alarms,alarms);
        }
    }

    public void addAlarm(AlarmInfo alarm) {
        this.alarms.add(alarm);
    }

    public void setXProps(Iterable <XProp> xProps) {
        this.xProps.clear();
        if (xProps != null) {
            Iterables.addAll(this.xProps,xProps);
        }
    }

    public void addXProp(XProp xProp) {
        this.xProps.add(xProp);
    }

    @Override
    public void setFragment(String fragment) { this.fragment = fragment; }
    @Override
    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }
    public void setOrganizer(CalOrganizer organizer) {
        this.organizer = organizer;
    }
    public void setRecurrence(RecurrenceInfo recurrence) {
        this.recurrence = recurrence;
    }
    public void setExceptionId(ExceptionRecurIdInfo exceptionId) {
        this.exceptionId = exceptionId;
    }
    public void setDtStart( DtTimeInfo dtStart) { this.dtStart = dtStart; }
    public void setDtEnd(DtTimeInfo dtEnd) { this.dtEnd = dtEnd; }
    public void setDuration(DurationInfo duration) { this.duration = duration; }

    @Override
    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }
    @Override
    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }
    @Override
    public List<String> getContacts() {
        return Collections.unmodifiableList(contacts);
    }
    public GeoInfo getGeo() { return geo; }
    public List<CalendarAttendee> getAttendees() {
        return Collections.unmodifiableList(attendees);
    }
    public List<AlarmInfo> getAlarms() {
        return Collections.unmodifiableList(alarms);
    }
    public List<XProp> getXProps() {
        return Collections.unmodifiableList(xProps);
    }
    @Override
    public String getFragment() { return fragment; }
    @Override
    public String getDescription() { return description; }
    @Override
    public String getHtmlDescription() { return htmlDescription; }
    public CalOrganizer getOrganizer() { return organizer; }
    @Override
    public CalOrganizerInterface getOrganizerInterface() { return organizer; }
    public RecurrenceInfo getRecurrence() { return recurrence; }
    @Override
    public RecurrenceInfoInterface getRecurrenceInterface() { return recurrence; }
    public ExceptionRecurIdInfo getExceptionId() { return exceptionId; }
    public DtTimeInfo getDtStart() { return dtStart; }
    public DtTimeInfo getDtEnd() { return dtEnd; }
    public DurationInfo getDuration() { return duration; }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("categories", categories)
            .add("comments", comments)
            .add("contacts", contacts)
            .add("geo", geo)
            .add("attendees", attendees)
            .add("alarms", alarms)
            .add("xProps", xProps)
            .add("fragment", fragment)
            .add("description", description)
            .add("htmlDescription", htmlDescription)
            .add("organizer", organizer)
            .add("recurrence", recurrence)
            .add("exceptionId", exceptionId)
            .add("dtStart", dtStart)
            .add("dtEnd", dtEnd)
            .add("duration", duration);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    @Override
    public void setGeoInterface(GeoInfoInterface geo) {
        setGeo((GeoInfo) geo);
    }

    @Override
    public void setAttendeeInterfaces(
            Iterable<CalendarAttendeeInterface> attendees) {
        setAttendees(CalendarAttendee.fromInterfaces(attendees));
    }

    @Override
    public void addAttendeeInterface(CalendarAttendeeInterface attendee) {
        addAttendee((CalendarAttendee) attendee);
    }

    @Override
    public void setAlarmInterfaces(Iterable<AlarmInfoInterface> alarms) {
        setAlarms(AlarmInfo.fromInterfaces(alarms));
    }

    @Override
    public void addAlarmInterface(AlarmInfoInterface alarm) {
        addAlarm((AlarmInfo) alarm);
    }

    @Override
    public void setXPropInterfaces(Iterable<XPropInterface> xProps) {
        setXProps(XProp.fromInterfaces(xProps));
    }

    @Override
    public void addXPropInterface(XPropInterface xProp) {
        addXProp((XProp) xProp);
    }

    @Override
    public void setOrganizerInterface(CalOrganizerInterface organizer) {
        setOrganizer((CalOrganizer) organizer);
    }

    @Override
    public void setRecurrenceInterface(RecurrenceInfoInterface recurrence) {
        setRecurrence((RecurrenceInfo) recurrence);
    }

    @Override
    public void setExceptionIdInterface(
            ExceptionRecurIdInfoInterface exceptionId) {
        setExceptionId((ExceptionRecurIdInfo) exceptionId);
    }

    @Override
    public void setDtStartInterface(DtTimeInfoInterface dtStart) {
        setDtStart((DtTimeInfo) dtStart);
    }

    @Override
    public void setDtEndInterface(DtTimeInfoInterface dtEnd) {
        setDtEnd((DtTimeInfo) dtEnd);
    }

    @Override
    public void setDurationInterface(DurationInfoInterface duration) {
        setDuration((DurationInfo) duration);
    }

    @Override
    public GeoInfoInterface getGeoInterface() {
        return this.geo;
    }

    @Override
    public List<CalendarAttendeeInterface> getAttendeeInterfaces() {
        return CalendarAttendee.toInterfaces(this.attendees);
    }

    @Override
    public List<AlarmInfoInterface> getAlarmInterfaces() {
        return AlarmInfo.toInterfaces(this.alarms);
    }

    @Override
    public List<XPropInterface> getXPropInterfaces() {
        return XProp.toInterfaces(this.xProps);
    }

    @Override
    public ExceptionRecurIdInfoInterface getExceptionIdInterface() {
        return this.exceptionId;
    }

    @Override
    public DtTimeInfoInterface getDtStartInterface() {
        return this.dtStart;
    }

    @Override
    public DtTimeInfoInterface getDtEndInterface() {
        return this.dtEnd;
    }

    @Override
    public DurationInfoInterface getDurationInterface() {
        return this.duration;
    }
}
