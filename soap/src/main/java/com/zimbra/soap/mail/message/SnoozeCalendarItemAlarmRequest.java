// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.SnoozeAlarm;
import com.zimbra.soap.mail.type.SnoozeAppointmentAlarm;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Snooze alarm(s) for appointments or tasks
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SNOOZE_CALITEM_ALARM_REQUEST)
public class SnoozeCalendarItemAlarmRequest {

  /**
   * @zm-api-field-description Details of alarms
   */
  @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = SnoozeAppointmentAlarm.class)
  private List<SnoozeAlarm> alarms = Lists.newArrayList();

  public SnoozeCalendarItemAlarmRequest() {}

  public void setAlarms(Iterable<SnoozeAlarm> alarms) {
    this.alarms.clear();
    if (alarms != null) {
      Iterables.addAll(this.alarms, alarms);
    }
  }

  public SnoozeCalendarItemAlarmRequest addAlarm(SnoozeAlarm alarm) {
    this.alarms.add(alarm);
    return this;
  }

  public List<SnoozeAlarm> getAlarms() {
    return Collections.unmodifiableList(alarms);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("alarms", alarms);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
