// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create a new Calendar Group
 */
@XmlRootElement(name = MailConstants.E_CREATE_CALENDAR_GROUP_REQUEST)
public class CreateCalendarGroupRequest {
  /** no-argument constructor wanted by JAXB */
  public CreateCalendarGroupRequest() {}

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Calendar Group Name
   */
  @XmlAttribute(name = "name" /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag calendarIds
   * @zm-api-field-description Calendar IDs
   */
  @XmlElement(name = "calendarId" /* calendarId */)
  private List<String> calendarIds;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCalendarIds() {
    return calendarIds;
  }

  public void setCalendarIds(List<String> calendarIds) {
    this.calendarIds = calendarIds;
  }
}
