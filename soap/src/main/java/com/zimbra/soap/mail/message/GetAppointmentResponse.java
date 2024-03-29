// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalendarItemInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "GetAppointmentResponse")
public class GetAppointmentResponse {

  /**
   * @zm-api-field-description Appointment information
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = CalendarItemInfo.class)
  })
  private final CalendarItemInfo item;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetAppointmentResponse() {
    this(null);
  }

  public GetAppointmentResponse(CalendarItemInfo item) {
    this.item = item;
  }

  public CalendarItemInfo getItem() {
    return item;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("item", item);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
