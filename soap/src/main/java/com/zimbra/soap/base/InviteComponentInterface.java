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
    InviteComponentInterface createFromMethodComponentNumRsvp(String method, int componentNum, boolean rsvp);
    void setCategories(Iterable<String> categories);
    void addCategory(String category);
    void setComments(Iterable<String> comments);
    void addComment(String comment);
    void setContacts(Iterable<String> contacts);
    void addContact(String contact);

    void setFragment(String fragment);
    void setDescription(String description);
    void setHtmlDescription(String htmlDescription);
    List<String> getCategories();
    List<String> getComments();
    List<String> getContacts();
    String getFragment();
    String getDescription();
    String getHtmlDescription();

    void setGeoInterface(GeoInfoInterface geo);
    void setAttendeeInterfaces(
        Iterable<CalendarAttendeeInterface> attendees);
    void addAttendeeInterface(CalendarAttendeeInterface attendee);
    void setAlarmInterfaces(Iterable<AlarmInfoInterface> alarms);
    void addAlarmInterface(AlarmInfoInterface alarm);
    void setXPropInterfaces(Iterable<XPropInterface> xProps);
    void addXPropInterface(XPropInterface xProp);
    void setOrganizerInterface(CalOrganizerInterface organizer);
    void setRecurrenceInterface(RecurrenceInfoInterface recurrence);
    void setExceptionIdInterface(
        ExceptionRecurIdInfoInterface exceptionId);
    void setDtStartInterface(DtTimeInfoInterface dtStart);
    void setDtEndInterface(DtTimeInfoInterface dtEnd);
    void setDurationInterface(DurationInfoInterface duration);
    GeoInfoInterface getGeoInterface();
    List<CalendarAttendeeInterface> getAttendeeInterfaces();
    List<AlarmInfoInterface> getAlarmInterfaces();
    List<XPropInterface> getXPropInterfaces();
    CalOrganizerInterface getOrganizerInterface();
    RecurrenceInfoInterface getRecurrenceInterface();
    ExceptionRecurIdInfoInterface getExceptionIdInterface();
    DtTimeInfoInterface getDtStartInterface();
    DtTimeInfoInterface getDtEndInterface();
    DurationInfoInterface getDurationInterface();
}
