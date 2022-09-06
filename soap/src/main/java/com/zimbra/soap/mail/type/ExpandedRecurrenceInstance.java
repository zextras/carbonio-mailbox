// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ExpandedRecurrenceInstance {

  /**
   * @zm-api-field-tag start-time-millis
   * @zm-api-field-description Start time in milliseconds
   */
  @XmlAttribute(name = MailConstants.A_CAL_START_TIME /* s */, required = false)
  private Long startTime;

  /**
   * @zm-api-field-tag duration-millies
   * @zm-api-field-description Duration in milliseconds
   */
  @XmlAttribute(name = MailConstants.A_CAL_NEW_DURATION /* dur */, required = false)
  private Long duration;

  /**
   * @zm-api-field-tag is-all-day
   * @zm-api-field-description Set if the instance is for an all day appointment
   */
  @XmlAttribute(name = MailConstants.A_CAL_ALLDAY /* allDay */, required = false)
  private ZmBoolean allDay;

  /**
   * @zm-api-field-tag tz-offset-millis
   * @zm-api-field-description GMT offset of start time in milliseconds; returned only when allDay
   *     is set
   */
  @XmlAttribute(name = MailConstants.A_CAL_TZ_OFFSET /* tzo */, required = false)
  private Integer tzOffset;

  /**
   * @zm-api-field-tag utc-recurrence-id
   * @zm-api-field-description Recurrence ID string in UTC timezone
   */
  @XmlAttribute(name = MailConstants.A_CAL_RECURRENCE_ID_Z /* ridZ */, required = false)
  private String recurIdZ;

  public ExpandedRecurrenceInstance() {}

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public void setAllDay(Boolean allDay) {
    this.allDay = ZmBoolean.fromBool(allDay);
  }

  public void setTzOffset(Integer tzOffset) {
    this.tzOffset = tzOffset;
  }

  public void setRecurIdZ(String recurIdZ) {
    this.recurIdZ = recurIdZ;
  }

  public Long getStartTime() {
    return startTime;
  }

  public Long getDuration() {
    return duration;
  }

  public Boolean getAllDay() {
    return ZmBoolean.toBool(allDay);
  }

  public Integer getTzOffset() {
    return tzOffset;
  }

  public String getRecurIdZ() {
    return recurIdZ;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("startTime", startTime)
        .add("duration", duration)
        .add("allDay", allDay)
        .add("tzOffset", tzOffset)
        .add("recurIdZ", recurIdZ);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
