// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = MailConstants.E_MODIFY_CALENDAR_GROUP_RESPONSE)
public class ModifyCalendarGroupResponse {
  /**
   * @zm-api-field-tag group
   * @zm-api-field-description Calendar Group Info
   */
  @XmlElement(name = "group" /* group */, required = true)
  private CalendarGroupInfo group;

  /** no-argument constructor wanted by JAXB */
  public ModifyCalendarGroupResponse() {}

  public CalendarGroupInfo getGroup() {
    return group;
  }

  public void setGroup(CalendarGroupInfo group) {
    this.group = group;
  }
}
