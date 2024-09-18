// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = MailConstants.E_CREATE_CALENDAR_GROUP_RESPONSE)
public class CreateCalendarGroupResponse extends CalendarGroupInfo {
  /** no-argument constructor wanted by JAXB */
  public CreateCalendarGroupResponse() {}
}
