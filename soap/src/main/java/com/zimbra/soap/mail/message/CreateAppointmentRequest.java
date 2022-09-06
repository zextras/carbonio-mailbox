// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalItemRequestBase;
import com.zimbra.soap.mail.type.Msg;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description This is the API to create a new Appointment, optionally sending out
 *     meeting Invitations to other people.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_CREATE_APPOINTMENT_REQUEST)
public class CreateAppointmentRequest extends CalItemRequestBase {
  public CreateAppointmentRequest() {
    super();
  }

  private CreateAppointmentRequest(Msg msg) {
    super(msg);
  }

  public static CreateAppointmentRequest create(Msg msg) {
    return new CreateAppointmentRequest(msg);
  }
}
