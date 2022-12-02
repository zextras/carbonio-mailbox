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
    public AlarmInfoInterface createFromAction(String action);
    public void setDescription(String description);
    public void setSummary(String summary);
    public String getAction();
    public String getDescription();
    public String getSummary();

    public void setTriggerInterface(AlarmTriggerInfoInterface trigger);
    public void setRepeatInterface(DurationInfoInterface repeat);
    public void setAttachInterface(CalendarAttachInterface attach);
    public void setAttendeeInterfaces(
            Iterable<CalendarAttendeeInterface> attendees);
    public void addAttendeeInterface(CalendarAttendeeInterface attendee);
    public void setXPropsInterface(Iterable<XPropInterface> xProps);
    public void addXPropInterface(XPropInterface xProp);
    public AlarmTriggerInfoInterface getTriggerInfo();
    public DurationInfoInterface getRepeatInfo();
    public CalendarAttachInterface getAttachInfo();
    public List<CalendarAttendeeInterface> getAttendeeInterfaces();
    public List<XPropInterface> getXPropInterfaces();
}
