// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.LegacyAppointmentData;
import com.zimbra.soap.mail.type.LegacyCalendaringData;
import com.zimbra.soap.mail.type.LegacyTaskData;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "GetTaskSummariesResponse")
public class GetTaskSummariesResponse {

  /**
   * @zm-api-field-description Task summary information
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = LegacyAppointmentData.class),
    @XmlElement(name = MailConstants.E_TASK /* task */, type = LegacyTaskData.class)
  })
  private List<LegacyCalendaringData> calEntries = Lists.newArrayList();

  public GetTaskSummariesResponse() {}

  public void setCalEntries(Iterable<LegacyCalendaringData> calEntries) {
    this.calEntries.clear();
    if (calEntries != null) {
      Iterables.addAll(this.calEntries, calEntries);
    }
  }

  public void addCalEntry(LegacyCalendaringData calEntry) {
    this.calEntries.add(calEntry);
  }

  public List<LegacyCalendaringData> getCalEntries() {
    return calEntries;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("calEntries", calEntries);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
