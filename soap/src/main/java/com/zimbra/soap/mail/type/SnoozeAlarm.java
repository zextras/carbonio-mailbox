// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class SnoozeAlarm {

  /**
   * @zm-api-field-tag cal-item-id
   * @zm-api-field-description Calendar item ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag new-alarm-time-millis
   * @zm-api-field-description When to show the alarm again in milliseconds since the epoch
   */
  @XmlAttribute(name = MailConstants.A_CAL_ALARM_SNOOZE_UNTIL /* until */, required = true)
  private final long snoozeUntil;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  protected SnoozeAlarm() {
    this((String) null, -1L);
  }

  public SnoozeAlarm(String id, long snoozeUntil) {
    this.id = id;
    this.snoozeUntil = snoozeUntil;
  }

  public String getId() {
    return id;
  }

  public long getSnoozeUntil() {
    return snoozeUntil;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("snoozeUntil", snoozeUntil);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
