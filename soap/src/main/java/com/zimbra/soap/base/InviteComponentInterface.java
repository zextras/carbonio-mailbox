// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface InviteComponentInterface
extends InviteComponentCommonInterface {
    public InviteComponentInterface createFromMethodComponentNumRsvp(String method, int componentNum, boolean rsvp);
    public void setCategories(Iterable <String> categories);
    public void addCategory(String category);
    public void setComments(Iterable <String> comments);
    public void addComment(String comment);
    public void setContacts(Iterable <String> contacts);
    public void addContact(String contact);

    public void setFragment(String fragment);
    public void setDescription(String description);
    public void setHtmlDescription(String htmlDescription);
    public List<String> getCategories();
    public List<String> getComments();
    public List<String> getContacts();
    public String getFragment();
    public String getDescription();
    public String getHtmlDescription();

    public void setGeoInterface(GeoInfoInterface geo);
    public void setAttendeeInterfaces(
            Iterable<CalendarAttendeeInterface> attendees);
    public void addAttendeeInterface(CalendarAttendeeInterface attendee);
    public void setAlarmInterfaces(Iterable<AlarmInfoInterface> alarms);
    public void addAlarmInterface(AlarmInfoInterface alarm);
    public void setXPropInterfaces(Iterable<XPropInterface> xProps);
    public void addXPropInterface(XPropInterface xProp);
    public void setOrganizerInterface(CalOrganizerInterface organizer);
    public void setRecurrenceInterface(RecurrenceInfoInterface recurrence);
    public void setExceptionIdInterface(
            ExceptionRecurIdInfoInterface exceptionId);
    public void setDtStartInterface(DtTimeInfoInterface dtStart);
    public void setDtEndInterface(DtTimeInfoInterface dtEnd);
    public void setDurationInterface(DurationInfoInterface duration);
    public GeoInfoInterface getGeoInterface();
    public List<CalendarAttendeeInterface> getAttendeeInterfaces();
    public List<AlarmInfoInterface> getAlarmInterfaces();
    public List<XPropInterface> getXPropInterfaces();
    public CalOrganizerInterface getOrganizerInterface();
    public RecurrenceInfoInterface getRecurrenceInterface();
    public ExceptionRecurIdInfoInterface getExceptionIdInterface();
    public DtTimeInfoInterface getDtStartInterface();
    public DtTimeInfoInterface getDtEndInterface();
    public DurationInfoInterface getDurationInterface();
}
