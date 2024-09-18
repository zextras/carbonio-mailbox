// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_CALENDAR_GROUPS_RESPONSE)
public class GetCalendarGroupsResponse {

  /**
   * @zm-api-field-description Calendar groups list
   */
  @XmlElement(name = "group" /* group */, required = true)
  private List<CalendarGroupInfo> groups;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetCalendarGroupsResponse() {}

  public List<CalendarGroupInfo> getGroups() {
    return groups;
  }

  public void setGroups(List<CalendarGroupInfo> groups) {
    this.groups = groups;
  }

}
