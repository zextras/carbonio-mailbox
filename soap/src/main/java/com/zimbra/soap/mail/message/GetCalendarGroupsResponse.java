// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_CALENDAR_GROUPS_RESPONSE)
public class GetCalendarGroupsResponse {

  /**
   * @zm-api-field-description Calendar groups list
   */
  @XmlElement(name = "groups" /* groups */, required = true)
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

  @XmlAccessorType(XmlAccessType.NONE)
  @XmlType(propOrder = {})
  public static class CalendarGroupInfo {
    /**
     * @zm-api-field-tag id
     * @zm-api-field-description Calendar Group ID
     */
    @XmlAttribute(name = "id" /* id */, required = true)
    private String id;

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
    @XmlAttribute(name = "calendarIds" /* calendarIds */, required = true)
    private List<String> calendarIds;

    /** no-argument constructor wanted by JAXB */
    @SuppressWarnings("unused")
    private CalendarGroupInfo() {}

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

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
}
