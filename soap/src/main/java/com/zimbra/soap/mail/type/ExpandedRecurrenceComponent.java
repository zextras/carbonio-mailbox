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
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ExpandedRecurrenceComponent {

  /**
   * @zm-api-field-description RECURRENCE_ID
   */
  @XmlElement(name = MailConstants.E_CAL_EXCEPTION_ID /* exceptId */, required = false)
  private InstanceRecurIdInfo exceptionId;

  /**
   * @zm-api-field-tag dtstart-millis
   * @zm-api-field-description DTSTART time in milliseconds since the Epoch
   */
  @XmlAttribute(name = MailConstants.A_CAL_START_TIME /* s */, required = false)
  private Long startTime;

  /**
   * @zm-api-field-tag dtend-millis
   * @zm-api-field-description DTEND time in milliseconds since the Epoch
   */
  @XmlAttribute(name = MailConstants.A_CAL_END_TIME /* e */, required = false)
  private Long endTime;

  /**
   * @zm-api-field-description DURATION
   */
  @XmlElement(name = MailConstants.E_CAL_DURATION /* dur */, required = false)
  private DurationInfo duration;

  /**
   * @zm-api-field-description RRULE/RDATE/EXDATE information
   */
  @XmlElement(name = MailConstants.E_CAL_RECUR /* recur */, required = false)
  private RecurrenceInfo recurrence;

  public ExpandedRecurrenceComponent() {}

  public void setExceptionId(InstanceRecurIdInfo exceptionId) {
    this.exceptionId = exceptionId;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public void setDuration(DurationInfo duration) {
    this.duration = duration;
  }

  public void setRecurrence(RecurrenceInfo recurrence) {
    this.recurrence = recurrence;
  }

  public InstanceRecurIdInfo getExceptionId() {
    return exceptionId;
  }

  public Long getStartTime() {
    return startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public DurationInfo getDuration() {
    return duration;
  }

  public RecurrenceInfo getRecurrence() {
    return recurrence;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("exceptionId", exceptionId)
        .add("startTime", startTime)
        .add("endTime", endTime)
        .add("duration", duration)
        .add("recurrence", recurrence);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
