// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.UpdatedAlarmInfo;
import com.zimbra.soap.mail.type.UpdatedAppointmentAlarmInfo;
import com.zimbra.soap.mail.type.UpdatedTaskAlarmInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="SnoozeCalendarItemAlarmResponse")
public class SnoozeCalendarItemAlarmResponse {

    /**
     * @zm-api-field-description Updated alarm information so the client knows when to trigger the next alarm
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_APPOINTMENT /* appt */, type=UpdatedAppointmentAlarmInfo.class),
        @XmlElement(name=MailConstants.E_TASK /* task */, type=UpdatedTaskAlarmInfo.class)
    })
    private List<UpdatedAlarmInfo> updatedAlarms = Lists.newArrayList();

    public SnoozeCalendarItemAlarmResponse() {
    }

    public void setUpdatedAlarms(Iterable <UpdatedAlarmInfo> updatedAlarms) {
        this.updatedAlarms.clear();
        if (updatedAlarms != null) {
            Iterables.addAll(this.updatedAlarms,updatedAlarms);
        }
    }

    public SnoozeCalendarItemAlarmResponse addUpdatedAlarm(
                    UpdatedAlarmInfo updatedAlarm) {
        this.updatedAlarms.add(updatedAlarm);
        return this;
    }

    public List<UpdatedAlarmInfo> getUpdatedAlarms() {
        return Collections.unmodifiableList(updatedAlarms);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("updatedAlarms", updatedAlarms);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
