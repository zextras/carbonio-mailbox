// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarItemRecur {

  /**
   * @zm-api-field-tag recurrence-id
   * @zm-api-field-description Information for iCalendar RECURRENCE-ID
   */
  @XmlElement(name = MailConstants.E_CAL_EXCEPTION_ID /* exceptId */, required = false)
  private ExceptionRecurIdInfo exceptionId;

  /**
   * @zm-api-field-description Start time
   */
  @XmlElement(name = MailConstants.E_CAL_START_TIME /* s */, required = false)
  private DtTimeInfo dtStart;

  /**
   * @zm-api-field-description End time
   */
  @XmlElement(name = MailConstants.E_CAL_END_TIME /* e */, required = false)
  private DtTimeInfo dtEnd;

  /**
   * @zm-api-field-description Duration information
   */
  @XmlElement(name = MailConstants.E_CAL_DURATION /* dur */, required = false)
  private DurationInfo duration;

  /**
   * @zm-api-field-description Recurrence information
   */
  @XmlElement(name = MailConstants.E_CAL_RECUR /* recur */, required = false)
  private RecurrenceInfo recurrence;

  public CalendarItemRecur() {}

  public void setExceptionId(ExceptionRecurIdInfo exceptionId) {
    this.exceptionId = exceptionId;
  }

  public void setDtStart(DtTimeInfo dtStart) {
    this.dtStart = dtStart;
  }

  public void setDtEnd(DtTimeInfo dtEnd) {
    this.dtEnd = dtEnd;
  }

  public void setDuration(DurationInfo duration) {
    this.duration = duration;
  }

  public void setRecurrence(RecurrenceInfo recurrence) {
    this.recurrence = recurrence;
  }

  public ExceptionRecurIdInfo getExceptionId() {
    return exceptionId;
  }

  public DtTimeInfo getDtStart() {
    return dtStart;
  }

  public DtTimeInfo getDtEnd() {
    return dtEnd;
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
        .add("dtStart", dtStart)
        .add("dtEnd", dtEnd)
        .add("duration", duration)
        .add("recurrence", recurrence);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
