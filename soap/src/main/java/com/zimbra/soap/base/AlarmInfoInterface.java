// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface AlarmInfoInterface {
    AlarmInfoInterface createFromAction(String action);
    void setDescription(String description);
    void setSummary(String summary);
    String getAction();
    String getDescription();
    String getSummary();

    void setTriggerInterface(AlarmTriggerInfoInterface trigger);
    void setRepeatInterface(DurationInfoInterface repeat);
    void setAttachInterface(CalendarAttachInterface attach);
    void setAttendeeInterfaces(
        Iterable<CalendarAttendeeInterface> attendees);
    void addAttendeeInterface(CalendarAttendeeInterface attendee);
    void setXPropsInterface(Iterable<XPropInterface> xProps);
    void addXPropInterface(XPropInterface xProp);
    AlarmTriggerInfoInterface getTriggerInfo();
    DurationInfoInterface getRepeatInfo();
    CalendarAttachInterface getAttachInfo();
    List<CalendarAttendeeInterface> getAttendeeInterfaces();
    List<XPropInterface> getXPropInterfaces();
}
