// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Msg;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Decline a change proposal from an attendee. Sent by organizer to an
 *     attendee who has previously sent a COUNTER message. The syntax of the request is very similar
 *     to CreateAppointmentRequest.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_DECLINE_COUNTER_APPOINTMENT_REQUEST)
public class DeclineCounterAppointmentRequest {

  /**
   * @zm-api-field-description Details of the Decline Counter. Should have an <b>&lt;inv></b> which
   *     encodes an iCalendar DECLINECOUNTER object
   */
  @XmlElement(name = MailConstants.E_MSG /* m */, required = false)
  private Msg msg;

  public DeclineCounterAppointmentRequest() {}

  public void setMsg(Msg msg) {
    this.msg = msg;
  }

  public Msg getMsg() {
    return msg;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("msg", msg);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
